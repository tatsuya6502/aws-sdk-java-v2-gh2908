package chunk.tester;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Random;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Command(name = "Chunk Tester", version = "Chunk Tester 1.0", mixinStandardHelpOptions = true)
public class App implements Runnable {

    // CLI options and parameters
    // https://picocli.info/quick-guide.html
    @Option(names = { "-H" }, paramLabel = "USE-HTTP", description = "Use HTTP (80) instead of HTTPS (443)")
    private boolean useHttp = false;

    @Option(names = { "-b", "--bucket" }, paramLabel = "BUCKET", description = "Bucket name")
    private String bucketName = "";

    // Asia Pacific (Tokyo)
    private static final String REGION = "ap-northeast-1";

    // Endpoint URL
    // See: https://docs.aws.amazon.com/general/latest/gr/s3.html
    private static final String HTTP_ENDPOINT = "http://s3.ap-northeast-1.amazonaws.com";

    private static final String KEY1 = "key1";
    private static final String KEY2 = "key2";
    private static final int OBJECT_SIZE = 512; // bytes

    public static void main(String[] args) {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        if (isEmptyString(bucketName)) {
            System.err.println("Please specify the bucket name using `-b` option.");
            System.exit(-1);
        }

        S3Client client = createS3Client();

        if (useHttp) {
            System.out.format("Connecting to %s region using HTTP endpoint %s.%n%n", REGION, HTTP_ENDPOINT);
        } else {
            System.out.format("Connecting to %s region using the default HTTPS endpoint.%n%n", REGION);
        }

        if (bucketExists(client, bucketName)) {
            System.out.format("Using existing bucket \"%s\".%n%n", bucketName);
        } else {
            createBucket(client, bucketName);
            System.out.format("Created bucket \"%s\".%n%n", bucketName);
        }

        System.out.println();

        int contentLength = OBJECT_SIZE / 2;

        System.out.format("%nPutObject: Sending a request. (key: %s, object size: %d, content length: %d)%n%n",
                KEY1, OBJECT_SIZE, contentLength);
        try {
            PutObjectResponse putObjectResp = putObject(client, bucketName, KEY1, OBJECT_SIZE, contentLength);
            System.out.format("%nPutObject: Received an OK response (status code: %d)%n%n",
                    putObjectResp.sdkHttpResponse().statusCode());
        } catch (S3Exception ex) {
            System.err.format("%nPutObject: Received an error response: %s%n%n", ex);
            ex.printStackTrace();
        }

        contentLength = OBJECT_SIZE;

        System.out.format("%nPutObject: Sending a request. (key: %s, object size: %d, content length: %d)%n%n",
                KEY2, OBJECT_SIZE, contentLength);
        try {
            PutObjectResponse putObjectResp = putObject(client, bucketName, KEY2, OBJECT_SIZE, contentLength);
            System.out.format("%nPutObject: Received an OK response (status code: %d)%n%n",
                    putObjectResp.sdkHttpResponse().statusCode());
        } catch (S3Exception ex) {
            System.err.format("%nPutObject: Received an error response: %s%n%n", ex);
            ex.printStackTrace();
        }
    }

    private S3Client createS3Client() {
        S3ClientBuilder client_builder = S3Client.builder().region(Region.of(REGION));

        if (useHttp) {
            client_builder.endpointOverride(URI.create(HTTP_ENDPOINT));
        }
                
        return client_builder.build();
    }

    private boolean bucketExists(S3Client client, String bucketName) {
        HeadBucketRequest headBucketRequest = HeadBucketRequest.builder().bucket(bucketName).build();
        try {
            client.headBucket(headBucketRequest);
            return true;
        } catch (S3Exception ex) {
            if (ex.statusCode() == 404) {
                return false;
            } else {
                throw ex;
            }
        }
    }

    private void createBucket(S3Client client, String bucketName) {
        CreateBucketRequest createBucketRequest = CreateBucketRequest.builder().bucket(bucketName).build();
        client.createBucket(createBucketRequest);
    }

    private PutObjectResponse putObject(S3Client client, String bucketName, String key, int bufferSize,
            int contentLength) {
        byte[] buffer = createRandomBytes(bufferSize);
        InputStream is = new ByteArrayInputStream(buffer);
        RequestBody body = RequestBody.fromInputStream(is, contentLength);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        return client.putObject(putObjectRequest, body);
    }

    private byte[] createRandomBytes(int size) {
        Random rnd = new Random();
        byte[] bytes = new byte[size];
        rnd.nextBytes(bytes);
        return bytes;
    }

    private boolean isEmptyString(String s) {
        return s == null || s.trim().length() == 0;
    }
}
