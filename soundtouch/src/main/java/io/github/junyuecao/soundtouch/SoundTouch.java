package io.github.junyuecao.soundtouch;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Soundtouch Java wrapper
 */
public class SoundTouch {
    private static final String TAG = "SoundTouch";

    static {
        System.loadLibrary("native-lib");
        init(SoundTouch.class.getCanonicalName().replaceAll("\\.", "/"));
    }

    private long handle = 0;

    public SoundTouch() {
        handle = newInstance();
    }

    public synchronized void release() {
        handle = 0;
        deleteInstance(handle);
    }


    public void putSamples(short[] samples, int samplesCount) {
        if (handle == 0) {
            return;
        }
        putSamples(handle, samples, samplesCount);
    }

    public void putSamples(byte[] samples, int size) {
        if (handle == 0) {
            return;
        }
        short[] buffer = new short[size / 2];
        ByteBuffer.wrap(samples)
                .order(ByteOrder.LITTLE_ENDIAN)
                .asShortBuffer().get(buffer);
        putSamples(handle, buffer, size / 2);
    }


    public int receiveSamples(short[] outputSamples, int maxSampleCount) {
        if (handle == 0) {
            return 0;
        }

        return receiveSamples(handle, outputSamples, maxSampleCount);
    }

    public int receiveSamples(byte[] outputArray, int maxOutputSize) {
        if (handle == 0) {
            return 0;
        }

        short[] outputSamples = new short[maxOutputSize / 2];
        int samples = receiveSamples(handle, outputSamples, maxOutputSize / 2);

        ByteBuffer byteBuffer = ByteBuffer.allocate(maxOutputSize);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.asShortBuffer().put(outputSamples, 0, samples);
        byteBuffer.get(outputArray);
        return samples * 2;
    }


    public int receiveSamples(int maxSampleCount) {
        if (handle == 0) {
            return 0;
        }

        return receiveSamples(handle, maxSampleCount);
    }


    public void setRate(double rate) {
        if (handle == 0) {
            return;
        }
        setRate(handle, rate);
    }

    public void setPitch(double pitch) {
        if (handle == 0) {
            return;
        }
        setPitch(handle, pitch);
    }

    public void setTempo(double newTempo) {
        if (handle == 0) {
            return;
        }
        setTempo(handle, newTempo);
    }


    public void setRateChange(double rate) {
        if (handle == 0) {
            return;
        }
        setRateChange(handle, rate);
    }


    public void setTempoChange(double tempoChange) {
        if (handle == 0) {
            return;
        }
        setTempoChange(handle, tempoChange);
    }

    public void setPitchOctaves(double pitchOctaves) {
        if (handle == 0) {
            return;
        }
        setPitchOctaves(handle, pitchOctaves);
    }


    public void setPitchSemiTones(int pitchSemiTones) {
        if (handle == 0) {
            return;
        }
        setPitchSemiTones(handle, pitchSemiTones);
    }


    public void setPitchSemiTones(double pitchSemiTones) {
        if (handle == 0) {
            return;
        }
        setPitchSemiTones(handle, pitchSemiTones);
    }


    public void setChannels(int channels) {
        if (handle == 0) {
            return;
        }
        setChannels(handle, channels);
    }


    public void setSampleRate(int sampleRate) {
        if (handle == 0) {
            return;
        }
        setSampleRate(handle, sampleRate);
    }


    public double getInputOutputSampleRatio() {
        if (handle == 0) {
            return 1;
        }
        return getInputOutputSampleRatio(handle);
    }


    public void flush() {
        if (handle == 0) {
            return;
        }
        flush(handle);
    }

    /**
     * Returns number of samples currently available.
     */
    public int numSamples() {
        return numSamples(handle);
    }

    /**
     * Returns number of channels
     */
    public int numChannels() {
        return numChannels(handle);
    }

    /**
     * Returns number of unprocessed samples
     */
    public int numUnprocessedSamples() {
        return numUnprocessedSamples(handle);
    }

    /**
     * Returns nonzero if there aren't any samples available for outputting.
     */
    public int isEmpty() {
        return isEmpty(handle);
    }

    /**
     * Clears all the samples in the object's output and internal processing
     * buffers.
     */
    public void clear() {
        clear(handle);
    }


    /**
     * Get SoundTouch library version Id
     *
     * @return version id
     */
    public static native int getVersionId();

    /**
     * Get SoundTouch library version string
     *
     * @return version string
     */
    public static native String getVersionString();


    /// ------ native methods start ------

    private static native long newInstance();

    private native long deleteInstance(long handle);

    private native void putSamples(long handle, short[] samples, int samplesCount);

    private native int receiveSamples(long handle, short[] outputSamples, int maxSampleCount);

    private native int receiveSamples(long handle, int maxSampleCount);

    private native void setRate(long handle, double rate);

    private native void setPitch(long handle, double pitch);

    private native void setTempo(long handle, double newTempo);

    private native void setRateChange(long handle, double rate);

    private native void setTempoChange(long handle, double tempoChange);

    private native void setPitchOctaves(long handle, double pitchOctaves);

    private native void setPitchSemiTones(long handle, int pitchSemiTones);

    private native void setPitchSemiTones(long handle, double pitchSemiTones);

    private native void setChannels(long handle, int channels);

    private native void setSampleRate(long handle, int sampleRate);

    private native double getInputOutputSampleRatio(long handle);

    private native void flush(long handle);

    private native int numSamples(long handle);

    private native int numChannels(long handle);

    private native int numUnprocessedSamples(long handle);

    private native int isEmpty(long handle);

    private native void clear(long handle);

    private static native int init(String canonicalName);
}
