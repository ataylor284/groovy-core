/*
 * Copyright 2003-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package groovy.util

/**
 * A builder dedicated at generating a file directory structure from a
 * specification. For example, imagine that you want to create the following tree:
 * <pre>
 * src/
 *  |--- main
 *  |     |--- groovy
 *  |            |--- Foo.groovy
 *  |--- test
 *        |--- groovy
 *               |--- FooTest.groovy
 *
 * </pre>
 *
 * <p>Then you can create the structure using:</p>
 * <pre><code>
 *     def tree = new FileTreeBuilder()
 *     tree.dir('src') {
 *        dir('main') {
 *           dir('groovy') {
 *              file('Foo.groovy', 'println "Hello"')
 *           }
 *        }
 *        dir('test') {
 *           dir('groovy') {
 *              file('FooTest.groovy', 'class FooTest extends GroovyTestCase {}')
 *           }
 *        }
 *     }
 * </code></pre>
 *
 * <p>or with this shorthand syntax:</p>
 * <pre><code>
 *     def tree = new FileTreeBuilder()
 *     tree.src {
 *        main {
 *           groovy {
 *              'Foo.groovy'('println "Hello"')
 *           }
 *        }
 *        test {
 *           groovy {
 *              'FooTest.groovy'('class FooTest extends GroovyTestCase {}')
 *           }
 *        }
 *     }
 * </code></pre>

 */
class FileTreeBuilder {

    File baseDir

    FileTreeBuilder(File baseDir = new File('.')) {
        this.baseDir = baseDir
    }

    /**
     * Creates a file with the specified name and the text contents using the system default encoding.
     * @param name name of the file to be created
     * @param contents the contents of the file, written using the system default encoding
     * @return the file being created
     */
    File file(String name, String contents) {
        new File(baseDir, name) << contents
    }

    /**
     * Creates a file with the specified name and the specified binary contents
     * @param name name of the file to be created
     * @param contents the contents of the file
     * @return the file being created
     */
    File file(String name, byte[] contents) {
        new File(baseDir, name) << contents
    }

    /**
     * Creates a new empty directory
     * @param name the name of the directory to create
     * @return the created directory
     */
    File dir(String name) {
        def f = new File(baseDir, name)
        f.mkdirs()
        f
    }

    /**
     * Creates a new directory and allows to specify a subdirectory structure using the closure as a specification
     * @param name name of the directory to be created
     * @param cl specification of the subdirectory structure
     * @return the created directory
     */
    File dir(String name, Closure cl) {
        def oldBase = baseDir
        def newBase = dir(name)
        try {
            baseDir = newBase
            cl.delegate = this
            cl.resolveStrategy = Closure.DELEGATE_FIRST
            cl()
        } finally {
            baseDir = oldBase
        }
        newBase
    }


    def methodMissing(String name, args) {
        if (args.length==1) {
            def arg = args[0]
            if (arg instanceof Closure) {
                dir(name, arg)
            } else if (arg instanceof CharSequence) {
                file(name, args.toString())
            } else if (arg instanceof byte[]) {
                file(name, arg)
            }
        }

    }
}