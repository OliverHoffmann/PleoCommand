#include "NexusEmulation.h"
#include "BCIError.h"
#include <windows.h>
#include <time.h>

using std::endl;

// access only by g_thread after ResumeThread() !
static const int g_channels = 32;
static float g_peakPropbability = 0.01; // 0 ... 1
static int g_maxAmplitude = 100 * 1000; // in units
static int g_maxPeakLength = 2000; // in ms
static int g_maxGrad0Length = 2000; // in ms
static int g_maxGrad1Length = 2000; // in ms
static int g_maxNoise = 30 * 1000; // in units
static int g_samplerate;

static CallbackDelegate g_callback = 0;
static int g_status = 0; // 1 = initialized, 2 = started
static volatile HANDLE g_thread;

struct Peak {
	bool peaking;
	float amp;
	int peakLen, grad0Len, grad1Len; // in loops
	float grad0Inc, grad1Inc;
	float value;
	int step;
};

static inline float rand01() {
	return rand() / (float) RAND_MAX;
}

static inline float rand_11() {
	return (rand() / (float) (RAND_MAX / 2)) - 1;
}

static DWORD WINAPI callbackFeeder(LPVOID) {
	static float g_data[g_channels];
	static struct Peak peaks[g_channels];
	memset(peaks, 0, g_channels * sizeof(struct Peak));
	srand((unsigned) time(0));
	while (g_thread) {
		clock_t clk = clock();
		for (int i = 0; i < g_channels; ++i) {
			struct Peak *p = &peaks[i];
			float d = .0;
			switch (p->peaking) {
				case 0:
				if (i == 0 && rand01() < g_peakPropbability) {
					p->peaking = 1;
					p->amp = rand_11() * g_maxAmplitude;
					p->peakLen = rand01() * g_maxPeakLength * g_samplerate / 1000;
					p->grad0Len = rand01() * g_maxGrad0Length * g_samplerate / 1000;
					p->grad1Len = rand01() * g_maxGrad1Length * g_samplerate / 1000;
					p->grad0Inc = p->amp / p->grad0Len;
					p->grad1Inc = p->amp / p->grad1Len;
					p->value = 0;
					p->step = 0;
				}
				break;
				case 1:
				d = (p->value += p->grad0Inc);
				if (++p->step >= p->grad0Len) {
					p->step = 0;
					++p->peaking;
				}
				break;
				case 2:
				d = p->amp; // assert(p->value == p->amp)
				if (++p->step >= p->peakLen) {
					p->step = 0;
					++p->peaking;
				}
				break;
				case 3:
				d = (p->value -= p->grad1Inc);
				if (++p->step >= p->grad1Len)
				p->peaking = 0;
				break;
			}
			g_data[i] = d + rand_11() * g_maxNoise;
		}
		g_callback(1, g_channels, g_data);
		int delay = 1000 / g_samplerate - (clock() - clk) / (CLOCKS_PER_SEC / 1000);
		if (delay > 0)
		Sleep(delay);
	}
	return 0;
}

int setParameter(float peakProbability, int maxAmplitude, int maxPeakLength,
        int maxGrad0Length, int maxGrad1Length, int maxNoise) {
	if (g_status == 2) {
		bcierr << "Wrong state" << endl;
		return -1;
	}
	g_peakPropbability = peakProbability;
	g_maxAmplitude = maxAmplitude;
	g_maxPeakLength = maxPeakLength;
	g_maxGrad0Length = maxGrad0Length;
	g_maxGrad1Length = maxGrad1Length;
	g_maxNoise = maxNoise;
	return 0;
}

int emulInitNexus(CallbackDelegate del) {
	if (g_status == 2) {
		bcierr << "Wrong state" << endl;
		return -1;
	}
	g_thread = CreateThread(0, 0, callbackFeeder, 0, CREATE_SUSPENDED, 0);
	if (!g_thread) {
		bcierr << "Cannot create emulation thread" << endl;
		return -1;
	}
	g_callback = del;
	g_status = 1;
	return 0;
}

int emulStartNexus(int* samplerate) {
	if (g_status != 1) {
		bcierr << "Wrong state" << endl;
		return -1;
	}
	if (*samplerate > 2048)
		*samplerate = 2048;
	if (*samplerate < 1)
		*samplerate = 1;
	g_samplerate = *samplerate;
	if (ResumeThread(g_thread) == (unsigned) -1) {
		bcierr << "Cannot start emulation thread" << endl;
		return -1;
	}
	g_status = 2;
	return 0;
}

unsigned long emulStopNexus(void) {
	if (g_status != 2) {
		bcierr << "Wrong state" << endl;
		return -1;
	}
	g_thread = 0;
	g_status = 1;
	return 0;
}

