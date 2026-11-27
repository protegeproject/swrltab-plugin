# SWRLTab plugin for Protégé 6

SWRLTab and SQWRLTab provide rule and query editing/execution in Protégé Desktop.
This **`protege-6` / 2.1.5-SNAPSHOT** branch targets **unmodified Protégé
6.0.0-alpha-1** on `protege6/release-6.0.0-alpha-1`, using **OWLAPI 5.5.1** and
**JDK 17**. It is a development build, not a released update for Protégé 5.

## Build and load

Clone the matching SWRLAPI sibling repositories and follow the
[host reactor instructions](https://github.com/protegeproject/swrlapi-project/blob/protege-6/ops/runbook.md#prot%C3%A9g%C3%A9-6-plugin-experiment).
For unchanged host APIs already built/resolved, package current SWRLAPI sources from
the workspace root:

```sh
mvn -B -f swrlapi-project/pom.xml -Pcore,protege-plugin \
  -pl :swrltab-plugin -am package
```

This runs producer/plugin unit tests and creates
`target/swrltab-plugin-2.1.5-SNAPSHOT.jar`; the full core `verify` gate remains separate.
Place the jar in the matching Protégé distribution's `plugins` directory, ensuring
only one SWRLTab bundle is present, then restart Protégé. For validation, the
[packaged and graphical harnesses](https://github.com/protegeproject/swrlapi-project/blob/protege-6/ops/runbook.md#packaged-and-graphical-validation)
use isolated caches and a temporary copy of the host. Keep the host's original
OWLAPI, Guava and Jackson bundles; the runbook checks duplicate or substituted bundles.

Enable **Window → Tabs → SWRLTab** and **SQWRLTab**. For rules, use
**OWL+SWRL→Drools**, **Run Drools**, then **Drools→OWL**. For a quick query, enter
`swrlb:add(?sum, 2, 3) -> sqwrl:select(?sum)` and check the result is `5`.

## Contribute built-ins

The [registration guide](docs/built-in-registration.md) covers the exact extension
point, factory and library code, prefix rendering, OSGi imports, lifecycle,
collision/error behavior and a built contributor jar exercised in the real host.

## Compatibility and releases

The host's Aqua appearance-menu exception on macOS/JDK 17 is a known, separate
host limitation recorded in the runbook; it does not require host dependency
upgrades for the tested rule/query workflows. Plugin Manager release metadata is
maintained separately in the [release procedure](https://github.com/protegeproject/swrlapi-project/blob/protege-6/release/README.md).
The [wiki](https://github.com/protegeproject/swrltab-plugin/wiki) distinguishes this
branch from released Protégé 5 builds. Existing screenshots under `img/` illustrate
the older UI and are not evidence of the current alpha build.

[Build guide](https://github.com/protegeproject/swrlapi-project/blob/protege-6/README.md) · [Runbook](https://github.com/protegeproject/swrlapi-project/blob/protege-6/ops/runbook.md) · [Roadmap](https://github.com/protegeproject/swrlapi-project/blob/protege-6/ops/roadmap.md)

## License

[BSD 2-Clause](license.txt), copyright Stanford University.
