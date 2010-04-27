#ifndef NexusEmulationH
#define NexusEmulationH

typedef void (*CallbackDelegate)(int nSamples, int nChan, float* fData);

int setParameter(float peakProbability, int maxAmplitude, int maxPeakLength,
        int maxGrad0Length, int maxGrad1Length, int maxNoise);
int emulInitNexus(CallbackDelegate del);
int emulStartNexus(int* samplerate);
unsigned long emulStopNexus(void);

#endif
