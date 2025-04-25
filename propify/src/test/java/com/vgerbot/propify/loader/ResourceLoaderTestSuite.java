package com.vgerbot.propify.loader;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite for running all resource loader tests together.
 * 
 * This includes tests for:
 * - FileResourceLoader
 * - HTTPResourceLoader
 * - CompileTimeClasspathResourceLoader
 * - CompileTimeResourceLoaderProvider
 * - RuntimeResourceLoaderProvider
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    FileResourceLoaderTest.class,
    HTTPResourceLoaderTest.class,
    CompileTimeClasspathResourceLoaderTest.class,
    CompileTimeResourceLoaderProviderTest.class,
    RuntimeResourceLoaderProviderTest.class
})
public class ResourceLoaderTestSuite {
    // This class is empty - it's just a placeholder for the annotations
} 