package com.example.schoolproject;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    //Tests if the application context is correctly retrieved
    @Test
    public void useAppContext() {
        // Retrieves the context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        //Verifies the package name
        assertEquals("com.example.schoolproject", appContext.getPackageName());
    }
}