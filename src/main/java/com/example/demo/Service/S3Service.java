package com.example.demo.Service;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;


@RequiredArgsConstructor
@Service
public class S3Service {

    private final AmazonS3 amazonS3;
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;
    @Value("${cloud.aws.region.static}")
    private String region;
    @Value("${cloud.front.url}")
    private String cloudFrontURL;

    String uploadFile(MultipartFile file) throws IOException {
        String key = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        System.out.println("파일이름 확인");
        System.out.println(file.getName());
        System.out.println(file.getOriginalFilename());


            PutObjectRequest putObjectRequest =
                    new PutObjectRequest(bucketName, key, region)
                            .withCannedAcl(CannedAccessControlList.PublicRead);
//            amazonS3.putObject(putObjectRequest); // put image to S3

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(file.getContentType());
        objectMetadata.setContentLength(file.getSize());
        InputStream inputStream = file.getInputStream();
        amazonS3.putObject(new PutObjectRequest(bucketName, key, inputStream, objectMetadata)
                .withCannedAcl(CannedAccessControlList.PublicRead));
//            amazonS3.putObject(new PutObjectRequest(bucketName, key, file)); // put image to S3

        String saveURL = cloudFrontURL+key;
        return saveURL;
//        return amazonS3.getUrl(bucketName, key).toString();
    }


    public void deleteFile(String url) throws IOException {
        try{
            String keyName =  url.replaceFirst(cloudFrontURL, "");
            System.out.println("url : " + url);
            System.out.println("keyName : " + keyName);
            amazonS3.deleteObject(bucketName, keyName);
        }catch (SdkClientException e){
            throw new IOException("Error deleting file from S3", e);
        }
    }


}
