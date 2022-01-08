# Debug Message Types

## Data source and generation

The root source of the debug message type list is the file
[debug-message-type.json](../silabs-pti/debug-message-type.json) in this
repository.

This file lists the feature level and date, and then the
individual debug message types.

This JSON file is generated into java artifacts at build time using
the build command:
```
./gradlew createDebugMessageTypes
```
This will generate the DebugMessageType java enum, which is then used
in the code. This will also execute automatically when you use `./gradlew all`, so if you forget to manually run it, it will be executed for you. In short: JSON file and generated java enums should never be out of sync.

Note that this JSON file is a root source of other generations as well.
This data is generated into C/C++ codespace, and elsewhere, so please
be careful when maintaining it, as the format change might affect
generators beyond just this repo.

## Data maintenance

If you add new types following is expected:

- increase a featureLevel by one. This is a bit of metadata that can ensure in the downstream tooling that you have the latest version of this library, and provide fail-fast approach.
- set the featureDate to informative date. This date is mostly used to just print out helpful messages, for comparison purposes, the featureLevel should always be used.
- add the new type at the end, and make sure you pick a unique code and name. If you don't, there are unit tests to catch you. This library will fail to build if you add a duplicate code.
- DO NOT DELETE OR REPURPOSE ANY OF THE EXISTING CODES! If you don't need them any more, mark them as deprecated, but don't delete them. You have more than enough space within a 2-byte integer! (Said at the risk of sounding really silly 10 years from now, but we have not used more than what is there now in the past 17 years from the time of this writing....)