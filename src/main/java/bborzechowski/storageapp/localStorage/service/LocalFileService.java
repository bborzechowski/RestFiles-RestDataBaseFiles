package bborzechowski.storageapp.localStorage.service;

import bborzechowski.storageapp.localStorage.model.LocalFileManage;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class LocalFileService {

    private static final Logger logger = LoggerFactory.getLogger(LocalFileService.class);

    private ServletContext servletContext;
    private String uploads;

    public LocalFileService(ServletContext servletContext) {
        this.servletContext = servletContext;
        createContextDirectory();

    }

    private void createContextDirectory(){
       // uploads = servletContext.getRealPath("/uploads/");
        uploads = "/home/bogu/uploads/";
        logger.info("Path to uploded file: {}", uploads);

        if(Strings.isBlank(uploads)){
            logger.error("Path to file no configureted!");
        }

        Path path = Paths.get(uploads);
        if (Files.notExists(path)) {
            try {
                logger.info("Try to create directory: {}", path);
                Files.createDirectories(path);
            } catch (IOException e) {
                logger.info("Cannot create directory: {}. Exception: {}", path, e.getMessage());
            //    e.printStackTrace();
            }
            logger.info("Path created on: {}", path);
        }
    }

    public ResponseEntity<?> getFile(String fileName){

        Path path = Paths.get(uploads + fileName);
        Resource resource;
        try {
            resource = new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            logger.info("Cannot get resource: {}", e.getMessage());
            return ResponseEntity
                    .notFound()
                    .build();
        }

        File targetFile;
        try {
            targetFile = resource.getFile();
        } catch (IOException e) {
            logger.info("Cannot get file: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .build();
        }


        String contentType;
        try {
            contentType = Files.probeContentType(path);
        } catch (IOException e) {
            logger.info("Cannot get contentType: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .build();
        }
        return ResponseEntity
                .ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\""+ targetFile.getName() + "\"")
                .contentLength(targetFile.length())
                .body(resource);
    }

    public List<LocalFileManage> getFiles(){

        Stream<Path> files;
        try {
            files = Files.walk(Paths.get(uploads)).filter(Files::isRegularFile);
        } catch (IOException e) {
            logger.info("Cannot get files on path: {} Error: {}", uploads , e.getMessage());
            return null;
        }
        List<LocalFileManage> localFileManages = new ArrayList<>();
        files.forEach(f -> {
            BasicFileAttributes bs;
            try {
                 bs = Files.readAttributes(f.toAbsolutePath(), BasicFileAttributes.class);
            } catch (IOException e) {
                logger.error("Error while getting BasicFileAttributes from file: {}", e.getMessage());
                return;
            }
            String downloadUri = ServletUriComponentsBuilder
                    .fromCurrentContextPath() // dane o hoscie, porcie, protokole
                    .path("/api/v1/files/download/") //dane do endpointa
                    .path(f.getFileName().toString())
                    .toUriString();

            String deleteUri = ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/api/v1/files/delete//") //dane do endpointa
                    .path(f.getFileName().toString())
                    .toUriString();

            LocalFileManage localFileManage = new LocalFileManage();
            localFileManage.setName(f.getFileName().toString());
            localFileManage.setCreationTime(bs.creationTime().toString());
            localFileManage.setLastModifier(bs.lastModifiedTime().toString());
            localFileManage.setSize(bs.size());
            localFileManage.setDownloadUri(downloadUri);
            localFileManage.setDeleteUri(deleteUri);
            try {
                localFileManage.setFileType(Files.probeContentType(f.toAbsolutePath()));
            } catch (IOException e) {
                logger.error("Error while getting probeContentType from file: {}", e.getMessage());
                return;
            }

            localFileManages.add(localFileManage);

        });
        return localFileManages;
    }

    public ResponseEntity<String> uploadFile(MultipartFile file){
        Path path = Paths.get(uploads + file.getOriginalFilename());
        try {
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error("Error while getting file {} from input: {}", file.getOriginalFilename(), e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(file.getOriginalFilename(), HttpStatus.CREATED);
    }

    public ResponseEntity<String> deleteFile(String filename){
        File file = new File(uploads + filename);
        if(file.delete()){
            logger.info("Delete file: {}", file.getName());
            return new ResponseEntity<>("Delete file: " + file.getName(), HttpStatus.OK);
        }
        return new ResponseEntity<>("File not Found: " + file.getName(), HttpStatus.NOT_FOUND);

    }
}
