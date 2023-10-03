package com.authenticket.authenticket.controller;

import com.authenticket.authenticket.controller.response.GeneralApiResponse;
import com.authenticket.authenticket.service.Utility;
import com.authenticket.authenticket.service.impl.AmazonS3ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin(
        origins = {
                "${authenticket.frontend-production-url}",
                "${authenticket.frontend-dev-url}",
                "${authenticket.loadbalancer-url}"
        },
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE},
        allowedHeaders = {"Authorization", "Cache-Control", "Content-Type"},
        allowCredentials = "true"
)
@RequestMapping("/api/aws")
public class AmazonController extends Utility {

    private final AmazonS3ServiceImpl service;

    @Autowired
    public AmazonController(AmazonS3ServiceImpl service) {
        this.service = service;
    }


    @PostMapping("/uploadFile")
    public ResponseEntity<GeneralApiResponse<Object>>  fileUpload(@RequestParam(value = "file") MultipartFile file,
                                                          @RequestParam(value = "imageName") String imageName,
                                                          @RequestParam(value = "file-type") String fileType){
        return ResponseEntity.status(200).body(generateApiResponse(null, service.uploadFile(file, imageName, fileType)));
    }
    @DeleteMapping("/deleteFile")
    public ResponseEntity<GeneralApiResponse<Object>> fileDelete(@RequestParam(value = "imageName") String imageName,
                                             @RequestParam(value = "file-type") String fileType) {
        return ResponseEntity.status(200).body(generateApiResponse( null, service.deleteFile(imageName, fileType)));
    }

    @GetMapping("/displayFile")
    public ResponseEntity<GeneralApiResponse<Object>>  fileDisplay(@RequestParam(value = "imageName") String imageName,
                                              @RequestParam(value = "file-type") String fileType){
        return ResponseEntity.status(200).body(generateApiResponse( service.displayFile(imageName, fileType), "url generated"));
    }
}
