# Registering a third-party SWRL built-in library

This guide targets **SWRLTab 2.1.5-SNAPSHOT on the `protege-6` branch**, using
**unmodified Protégé 6.0.0-alpha-1 and JDK 17**. It describes the library registration
implemented in this branch; older Protégé 5 releases have different loading behavior.

## Start with the tested contributor

The executable example is maintained with the plugin tests:

- [ExampleLibrary.java](../src/test/java/org/swrltab/fixture/ExampleLibrary.java) declares prefix `contributed`, namespace `urn:swrlapi:contributed#`, and built-in `answer`.
- [ExampleLibraryFactory.java](../src/test/java/org/swrltab/fixture/ExampleLibraryFactory.java) creates a fresh library instance.
- [plugin.xml](../src/test/resources/contributor/plugin.xml) registers that factory with Protégé.
- [ContributedLibraryTest.java](../src/test/java/org/swrltab/core/ContributedLibraryTest.java) exercises registration, queries, inference and rejection cases.

`answer` binds an unbound argument to the integer `42`; a bound argument succeeds
only when its numeric value equals `42`. The fixture's counters and deliberate
failure switch support lifecycle tests; they are not required in a production library.
The [packager](https://github.com/protegeproject/swrlapi-project/blob/protege-6/ops/experiments/build-contributor-fixture.py)
uses these compiled classes directly, so the executable example and the tests share
one implementation.

## Library and factory contracts

Extend `org.swrlapi.builtins.AbstractSWRLBuiltInLibrary`, passing your prefix,
namespace and nonempty set of method names to its constructor. Use a unique
namespace ending in the separator you want before each local name, for example
`https://example.org/builtins#`. The prefix has no trailing colon.

Each declared built-in must be a public Java method returning primitive
`boolean`, accepting `List<SWRLBuiltInArgument>` and declaring exactly one exception, `SWRLBuiltInException`.
Use the library's argument-checking and result-binding helpers, as the example does;
a false result filters a match, while invalid arguments raise a built-in exception.
Implement `reset()` to clear per-library state and make repeated resets safe.

Provide a **public no-argument subclass** of `org.swrltab.core.SWRLBuiltInLibraryFactory`.
Override `createSWRLBuiltInLibrary()` to return a new, non-null library:

```java
package example;

import org.swrlapi.builtins.SWRLBuiltInLibrary;
import org.swrltab.core.SWRLBuiltInLibraryFactory;

public class MyLibraryFactory extends SWRLBuiltInLibraryFactory {
  @Override protected SWRLBuiltInLibrary createSWRLBuiltInLibrary() {
    return new MyLibrary();
  }
}
```

`MyLibrary` is your library implementation. Do not use a process-wide singleton
for a mutable contributed library. Extend the factory rather than instantiating
the base factory, whose creation hook deliberately reports an unconfigured contribution.

## Declare the extension and OSGi imports

Put `plugin.xml` at the root of your bundle. The extension point belongs to the
SWRLTab bundle, so its complete name is **`swrltab-plugin.SWRLBuiltInLibraryFactoryPlugin`**:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<plugin>
  <extension id="myLibrary" point="swrltab-plugin.SWRLBuiltInLibraryFactoryPlugin">
    <name value="My built-in library"/>
    <class value="example.MyLibraryFactory"/>
  </extension>
</plugin>
```

The extension ID identifies the contribution; it does not supply the built-in
prefix. The library's `getPrefix()` and `getNamespace()` supply that identity.
For the tested example, rendering and parsing use `contributed:answer` even if the
ontology has no prefix declaration or has a different alias for the same namespace.
Ordinary OWL entities continue to use the host's normal rendering.

A minimal contributor manifest contains these imports (manifest continuation
lines start with one space, and the file ends with a blank line):

```text
Manifest-Version: 1.0
Bundle-ManifestVersion: 2
Bundle-SymbolicName: example.swrl.builtins;singleton:=true
Bundle-Name: Example SWRL Built-ins
Bundle-Version: 1.0.0
Import-Package: org.swrltab.core;version="[2.1,3)",
 org.swrlapi.builtins;version="[2.1,3)",
 org.swrlapi.builtins.arguments;version="[2.1,3)",
 org.swrlapi.exceptions;version="[2.1,3)"

```

Add imports for any additional APIs you use. Compile against the matching SWRLTab
and SWRLAPI APIs, treating them as provided dependencies when building an external
Maven bundle. Include your classes and extension descriptor, **not another SWRLAPI,
OWLAPI or Protégé jar**. SWRLTab exports its shared API; Drools implementation
packages are private. Duplicate API copies can cause class-cast or method-linkage
failures despite identical class names. The fixture needs no custom bundle activator.

## Lifecycle and error handling

| Event | Contract |
| --- | --- |
| Discovery | Each tab's engine factory discovers identified extensions and orders them by extension ID. |
| Initialization | The plugin wrapper calls `initialise()`, creates a library and resets it before registration. |
| Engine creation | Fresh contributed instances are registered before the first ontology import, so saved rules and queries can resolve them. |
| Repeated factory initialization | Reuses that initialized factory's library; it does not create another instance. |
| Access before initialization or after disposal | `getSWRLBuiltInLibrary()` throws `IllegalStateException`. |
| Tab disposal or ontology switch | Old factories are disposed and old model listeners removed; the replacement engine has fresh instances. |
| Factory disposal | Clears the library reference and resets the library; repeated disposal is safe. |
| Registration failure | Tab creation reports the error and disposes factories created so far; a later valid ontology switch can recover. |

Prefixes, namespaces and built-in IRIs must be unique. Registration does not
replace an existing library. Every declared method is validated before that library's
registration becomes visible; a bad method cannot leave a partly registered library.
An invalid identity, empty method set, collision or invalid method raises
`IllegalArgumentException` in the manager. Null libraries and unconfigured factories
fail explicitly. The plugin wraps initialization/registration failures with the
contribution context and logs cleanup failures.

## Build and exercise the contributor

First follow the [sibling build guide](https://github.com/protegeproject/swrlapi-project/blob/protege-6/README.md)
and [host setup](https://github.com/protegeproject/swrlapi-project/blob/protege-6/ops/runbook.md#prot%C3%A9g%C3%A9-6-plugin-experiment).
With matching host dependencies already built, run from the SWRLAPI workspace:

```sh
mvn -B -f swrlapi-project/pom.xml -Pcore,protege-plugin \
  -pl :swrltab-plugin -am package
python3 swrlapi-project/ops/experiments/build-contributor-fixture.py \
  swrltab-plugin /tmp/swrlapi-contributor-fixture.jar
```

The fixture jar contains the library, factory, extension descriptor, manifest and
BSD license. It contains no duplicate API classes. Set `protege_dist` to the
extracted platform-independent **6.0.0-alpha-1** distribution and `JAVA_HOME` to JDK 17:

```sh
python3 swrlapi-project/ops/experiments/verify-host-bundles.py "$protege_dist"
"$JAVA_HOME/bin/java" -cp "$protege_dist/bundles/org.apache.felix.main.jar" \
  swrlapi-project/ops/experiments/PluginSmoke.java \
  "$protege_dist" swrltab-plugin/target/swrltab-plugin-2.1.5-SNAPSHOT.jar \
  /tmp/swrlapi-plugin-smoke.ttl /tmp/swrlapi-contributor-fixture.jar
```

The packaged check must show an ACTIVE SWRLTab bundle, correct shared API identity,
one contributed result `42`, no result for the negative control, and rule inference
of `ContributedAdult(alice)`. It also writes the fixture ontology used by the GUI check.
The inventory check alone proves neither rule execution nor graphical behavior.

In a desktop session, run the actual host's Swing workflow against a temporary copy:

```sh
python3 swrlapi-project/ops/experiments/build-contributor-fixture.py \
  swrltab-plugin /tmp/swrlapi-graphical-fixture.jar --graphical
python3 swrlapi-project/ops/experiments/run-graphical-smoke.py \
  "$protege_dist" swrltab-plugin/target/swrltab-plugin-2.1.5-SNAPSHOT.jar \
  /tmp/swrlapi-graphical-fixture.jar /tmp/swrlapi-plugin-smoke.ttl \
  --java-home "$JAVA_HOME"
```

The harness checks query results, edits, contributed rules, ontology switches,
failed initialization, tab reopening and cleanup. It uses a separate home/cache
and prints the loaded artifact hashes. Its deliberate failure cases produce
expected errors; success ends with `GRAPHICAL PLUGIN PASS`. This is an automated
Swing workflow in the running host, not a native mouse-routing test.

For manual verification, install the plugin and non-graphical contributor in a
separate test distribution, open the generated ontology and enable **Window → Tabs →
SWRLTab / SQWRLTab**. Run `contributedAnswer` and expect `42`; run
`contributed:answer(41) -> sqwrl:select(41)` and expect no rows. Import, run and export
the contributed rule, then inspect `ContributedAdult(alice)`. Remove the test fixture
when finished; the graphical fixture is test instrumentation, not a production extension.

## Plain Java applications

Protégé extension discovery applies only inside the plugin. For a Java application,
subclass `DefaultSWRLRuleAndQueryEngineFactory`, override its protected
`configureBuiltInLibraries(SWRLBuiltInLibraryManager, IRIResolver)` hook, and call
`manager.registerSWRLBuiltInLibrary(new MyLibrary())` there. Create engines through
that factory so registration happens before ontology import; first call its
`tryToRegisterADefaultSWRLRuleEngine()` to discover the engine implementation. The standalone SWRLTab
application does not discover `plugin.xml` contributions automatically.

[BSD 2-Clause license](../license.txt).
