# SWRLTab plugin for Protégé 6

SWRLTab and SQWRLTab provide rule and query editing/execution in Protégé Desktop.
This **`protege-6` / 2.1.5-SNAPSHOT** branch targets **unmodified Protégé
6.0.0-alpha-1** on `protege6/release-6.0.0-alpha-1`, using **OWLAPI 5.5.1** and
**JDK 17**. It is a development build, not a released update for Protégé 5.

## Update channels and host compatibility

Protégé uses a plugin registry listing `update.properties` files. The Protégé 5
channel stays on SWRLTab **2.1.4**, with its jar and readme pinned to that release.
The separate Protégé 6 template is labelled **SWRLTab (Protege 6 only)** and must
be registered only in the Protégé 6 registry after a verified release is uploaded.
This snapshot is not advertised as an update in either registry.

The updater does not filter these properties by host version. Feed separation
prevents the normal Protégé 5 update path from offering the new plugin; mandatory
OSGi imports in `[6.0.0,7.0.0)` prevent it loading if someone manually copies it
into Protégé 5. Adding an unsupported minimum-version property would not protect
the download path. CI guards the legacy feed, release URLs, explicit host label,
and mandatory host imports; release verification checks the packaged manifest too.

The unmodified Protégé 6.0.0-alpha-1 still defaults to the Protégé 5 registry and
preference key. This is recorded for its maintainers in the
[roadmap](https://github.com/protegeproject/swrlapi-project/blob/protege-6/ops/roadmap.md).
Public registration of this plugin requires the correct Protégé 6 registry;
never repoint the Protégé 5 feed to make the alpha host discover it.

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
