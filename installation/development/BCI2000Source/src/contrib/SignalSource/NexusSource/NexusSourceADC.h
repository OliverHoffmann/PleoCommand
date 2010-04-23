#ifndef NexusSourceADCH
#define NexusSourceADCH

#include "GenericADC.h"
#include <time.h>

typedef CDECL void (*CallbackDelegate)(int nSamples, int nChan, float* fData);
typedef CDECL int (*InitNeXusDevice)(CallbackDelegate del);
typedef CDECL int (*StartNeXusDevice)(int* samplerate);
typedef CDECL unsigned long (*StopNeXusDevice)();

#define MIN	0
#define MAX	1
#define SUM	2

class NexusSourceADC: public GenericADC {
public:
	NexusSourceADC();
	virtual ~NexusSourceADC();

	virtual void Preflight(const SignalProperties&, SignalProperties&) const;
	virtual void Initialize(const SignalProperties&, const SignalProperties&);
	virtual void Process(const GenericSignal&, GenericSignal&);
	virtual void Halt();

	void callBack(int, int, float*);

private:
	InitNeXusDevice initNexus;
	StartNeXusDevice startNexus;
	StopNeXusDevice stopNexus;
	bool m_emulationSelected;

	HANDLE m_mutex;
	float *m_data; // needs mutex protection
	int m_sampleIndex; // needs mutex protection
	int m_dropped; // needs mutex protection
	int m_lastDropped;

	bool m_started;

	int m_channels;
	int m_elements;
	int m_samplingRate;
	bool m_emulate;
	int m_statistics;
	clock_t m_lastStats;

	int m_statCount;
	int m_statSamples[3];
	int m_statChannels[3];
	float m_statValues[32][3];
};

#endif
