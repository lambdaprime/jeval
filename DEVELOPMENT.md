# Build

Building  module locally and making changes to it (this is optional and not intended for users).

## With Gradle

``` bash
gradle clean build
```

## With Maven

``` bash
mvn clean install
```

Integration tests are disable by default and activated with separate profile:

``` bash
mvn clean install -Pintegration-test
```

## With Eclipse

- Build eclipse projects:

``` bash
gradle eclipse
```

- Import them into Eclipse

## Standalone application

To build standalone application which would include all its dependencies:

``` bash
mvn clean install -Papp
```

# Release steps

- Close version in gradle.properties
- Run `gradle clean build javadoc`
- Publish
- Open next SNAPSHOT version
- Update CHANGELOG.md with new release (for changelog generation use `git log --format=%s`)
- Commit changes
- Push
- Upload documentation to website
