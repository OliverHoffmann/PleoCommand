#include "PCHIncludes.h"
#pragma hdrstop

#include "NexusSourceADC.h"
#include "NexusEmulation.h"
#include <float.h>

using std::endl;

void callBack(int samples, int channels, float* data); // forward

static NexusSourceADC *g_nexusADC;
RegisterFilter(NexusSourceADC, 1);

NexusSourceADC::NexusSourceADC() :
	m_emulationSelected(false), initNexus(0), startNexus(0), stopNexus(0),
	        m_mutex(CreateMutex(0, 0, 0)), m_data(0), m_sampleIndex(-1),
	        m_dropped(0), m_lastDropped(0), m_started(false), m_channels(0),
	        m_elements(0), m_samplingRate(0), m_emulate(false),
	        m_statistics(10), m_lastStats(0) {

	g_nexusADC = this;

	BEGIN_PARAMETER_DEFINITIONS
		"Source int SourceCh= 16 16 1 32"
		"// the number of digitized and stored channels",
		"Source int SampleBlockSize= 32 32 1 64"
		"// the number of samples per channel",
		"Source int SamplingRate= 512 512 32 2048"
		"// the sampling rate (in Hz)",
		"Source int Emulate= 0 0 0 1"
		"// only emulate NeXus access? (boolean)",
		"Source float PeakProbability= 0.01 0.01 0 1"
		"// probability for a peak",
		"Source int MaxAmplitude= 100000 100000 0 %"
		"// maximal height of peak",
		"Source int MaxPeakLength= 2000 2000 0 %"
		"// maximal length of peak (in ms)",
		"Source int MaxGrad0Length= 2000 2000 0 %"
		"// maximal length of raising gradient (in ms)",
		"Source int MaxGrad1Length= 2000 2000 0 %"
		"// maximal length of falling gradient (in ms)",
		"Source int MaxNoise= 30000 30000 0 %"
		"// maximal amount of noise",
		"Source int Statistics= 10 10 0 3600"
		"// log statistics every n seconds to debug output (0 == off)",
	END_PARAMETER_DEFINITIONS

	BEGIN_STATE_DEFINITIONS
		"Running 1 0 0 0",
	END_STATE_DEFINITIONS
}

NexusSourceADC::~NexusSourceADC() {
	Halt();
	CloseHandle(m_mutex);
	free(m_data);
	g_nexusADC = 0;
}

void NexusSourceADC::Preflight(const SignalProperties&,
        SignalProperties& outSignalProperties) const {
	// Requested output signal properties.
	outSignalProperties = SignalProperties(Parameter("SourceCh"), Parameter(
	        "SampleBlockSize"), SignalType::float32);

}

void NexusSourceADC::Initialize(const SignalProperties&,
        const SignalProperties&) {

	m_channels = Parameter("SourceCh");
	m_elements = Parameter("SampleBlockSize");
	m_samplingRate = Parameter("SamplingRate");
	m_emulate = (int) Parameter("Emulate");
	m_statistics = Parameter("Statistics");
	if (m_emulate)
		setParameter(Parameter("PeakProbability"), Parameter("MaxAmplitude"),
		        Parameter("MaxPeakLength"), Parameter("MaxGrad0Length"),
		        Parameter("MaxGrad1Length"), Parameter("MaxNoise"));

	if (m_emulationSelected ^ m_emulate) {
		// need to switch if already initialized
		initNexus = NULL;
		startNexus = NULL;
		stopNexus = NULL;
	}
	m_emulationSelected = m_emulate;
	if (!initNexus || !startNexus || !stopNexus) {
		if (m_emulate) {
			initNexus = emulInitNexus;
			startNexus = emulStartNexus;
			stopNexus = emulStopNexus;
		} else {
			HINSTANCE inst = LoadLibrary("NeXusDLL.dll");
			if (!inst) {
				bcierr << "Cannot load library NeXusDLL.dll" << endl;
				return;
			}
			initNexus = (InitNeXusDevice) GetProcAddress(inst,
			        "InitNeXusDevice");
			startNexus = (StartNeXusDevice) GetProcAddress(inst,
			        "StartNeXusDevice");
			stopNexus = (StopNeXusDevice) GetProcAddress(inst,
			        "StopNeXusDevice");
			if (!initNexus || !startNexus || !stopNexus) {
				bcierr << "Cannot load functions in library NeXusDLL.dll"
				        << endl;
				return;
			}
		}
	}

	int res = initNexus(::callBack);
	if (res) {
		bcierr << "InitNeXusDevice failed: " << res << endl;
		return;
	}

	// using realloc here in case it has already been allocated
	m_data = (float*) realloc(m_data, m_channels * m_elements * sizeof(float));
	m_sampleIndex = -1;
	m_dropped = 0;
	m_lastDropped = 0;

	res = startNexus(&m_samplingRate);
	if (res)
		bcierr << "StartNeXusDevice failed: " << res << endl;
	m_started = true;

	if (m_statistics) {
		m_statCount = 0;
		m_lastStats = clock() / CLOCKS_PER_SEC;
		m_statSamples[MIN] = INT_MAX;
		m_statSamples[MAX] = INT_MIN;
		m_statSamples[SUM] = 0;
		m_statChannels[MIN] = INT_MAX;
		m_statChannels[MAX] = INT_MIN;
		m_statChannels[SUM] = 0;
		for (int i = 0; i < 32; ++i) {
			m_statValues[i][MIN] = FLT_MAX;
			m_statValues[i][MAX] = FLT_MIN;
			m_statValues[i][SUM] = 0;
		}
	}
}

void callBack(int samples, int channels, float* data) {
	g_nexusADC->callBack(samples, channels, data);
}

static inline int min(int i1, int i2) {
	return i1 < i2 ? i1 : i2;
}

static inline int max(int i1, int i2) {
	return i1 > i2 ? i1 : i2;
}

void NexusSourceADC::callBack(int samples, int channels, float* data) {
	WaitForSingleObject(m_mutex, INFINITE);
	if (m_sampleIndex == m_elements - 1) {
		++m_dropped;
		ReleaseMutex(m_mutex);
		return;
	}
	++m_sampleIndex;
	int i = -1;
	for (int c = 0; c < m_channels; ++c) {
		*(m_data + c * m_elements + m_sampleIndex) = data[++i];
		if (m_statistics) {
			float d = data[i];
			m_statValues[i][MIN] = min(m_statValues[i][MIN], d);
			m_statValues[i][MAX] = max(m_statValues[i][MAX], d);
			m_statValues[i][SUM] += d;
		}
	}

	if (m_statistics) {
		m_statCount += samples;
		m_statSamples[MIN] = min(m_statSamples[MIN], samples);
		m_statSamples[MAX] = max(m_statSamples[MAX], samples);
		m_statSamples[SUM] += samples;
		m_statChannels[MIN] = min(m_statChannels[MIN], channels);
		m_statChannels[MAX] = max(m_statChannels[MAX], channels);
		m_statChannels[SUM] += channels;
	}

	ReleaseMutex(m_mutex);
}

void NexusSourceADC::Process(const GenericSignal&, GenericSignal& signal) {
	while (true) {
		WaitForSingleObject(m_mutex, INFINITE);
		if (m_sampleIndex >= m_elements - 1)
			break; // keep mutex
		ReleaseMutex(m_mutex);
		// need to wait for more samples
		Sleep(1);
	}
	for (int c = 0; c < m_channels; ++c)
		for (int e = 0; e < m_elements; ++e)
			signal(c, e) = *(m_data + c * m_elements + e);
	m_sampleIndex = -1;

	if (m_statistics) {
		clock_t now = clock() / CLOCKS_PER_SEC;
		if (now - m_lastStats >= m_statistics) {
			m_lastStats = now;
			bcidbg << "Channel-Stats: " << m_statChannels[MIN] << " - "
			        << m_statChannels[MAX] << " [" << (m_statChannels[SUM]
			        / (float) m_statCount) << "]" << endl;
			bcidbg << "Sample-Stats: " << m_statSamples[MIN] << " - "
			        << m_statSamples[MAX] << " [" << (m_statSamples[SUM]
			        / (float) m_statCount) << "]" << endl;
			for (int i = 0; i < m_channels; ++i) {
				bcidbg << "Value-Stats: <" << i << "> " << m_statValues[i][MIN]
				        << " - " << m_statValues[i][MAX] << " ["
				        << (m_statValues[i][SUM] / m_statCount) << "]" << endl;
			}
			if (m_dropped != m_lastDropped) {
				bcidbg << "Dropped " << (m_dropped - m_lastDropped)
				        << " sample(s)" << endl;
				m_lastDropped = m_dropped;
			}
		}
	}

	ReleaseMutex(m_mutex);
}

void NexusSourceADC::Halt() {
	if (m_started) {
		WaitForSingleObject(m_mutex, INFINITE);
		if (m_dropped % 100)
			bciout << "Summary: Dropped " << m_dropped << " sample(s)" << endl;
		unsigned long res = stopNexus();
		if (res)
			bcierr << "StopNeXusDevice failed: " << res << endl;
		else
			m_started = false;
		ReleaseMutex(m_mutex);
	}
}

