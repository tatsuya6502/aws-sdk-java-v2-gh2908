# Chunk Tester for GitHub Issue 2908 (AWS SDK Java 2)

This is an example program to reproduce the issue described in:

- https://github.com/aws/aws-sdk-java-v2/issues/2908

It does the followings:

1. Create a bucket with a given bucket name if not present.
2. Put an object with key "key1" to the bucket using a wrong content length.
3. Put an object with key "key2" to the bucket using the correct content length.

[aws-sdk-java2]: https://github.com/aws/aws-sdk-java-v2


## Requirements

- A Java SDK
- Linux, macOS or Windows. (Not tested on Windows)
- An Internet connection


## Running the tool

Build and run the program by the following commands:

```console
## Linux or macOS

# Run the tool using the default HTTPS (443) protocol.
$ ./gradlew run --args='--bucket mybucket'

# Runt the tool using HTTP (80) protocol.
$ ./gradlew run --args='--bucket mybucket -H'
```


## Other Configurations

### Changing the AWS region

Edit `app/src/main/java/chunk/tester/App.java` and change the `REGION` and
`HTTP_ENDPOINT`.

```java
// Asia Pacific (Tokyo)
private static final String REGION = "ap-northeast-1";

// Endpoint URL
// See: https://docs.aws.amazon.com/general/latest/gr/s3.html
private static final String HTTP_ENDPOINT = "http://s3.ap-northeast-1.amazonaws.com";
```


### Changing the version of AWS SDK for Java

Edit `app/build.gradle` and change the `awsSdkVersion`.

```groovy
// The version of the AWS SDK for Java.
def awsSdkVersion = '2.17.122'
```
