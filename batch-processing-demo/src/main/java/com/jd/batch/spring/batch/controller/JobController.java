package com.jd.batch.spring.batch.controller;

import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by jd birla on 03-02-2023 at 06:49
 */
@RestController
@RequestMapping("/jobs")
public class JobController {

    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    private Job job;
    private  final String  TEMP_STORAGE = "C:\\D_Drive\\DXC\\Learning\\Projects\\JD_master_Spring_Batch\\tempfile";

    @PostMapping("/importCutomers")
    public void importCsvToDbJob(@RequestParam("file")MultipartFile multipartFile) throws IOException {
        String originalFileName = multipartFile.getOriginalFilename();
        File fileToImport =  new File(TEMP_STORAGE+originalFileName);
        multipartFile.transferTo(fileToImport);
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("fullPathFileName" , TEMP_STORAGE+originalFileName)
                .addLong("starAt" , System.currentTimeMillis()).toJobParameters();
        try {
            JobExecution runStatus = jobLauncher.run(job, jobParameters);

            if(runStatus.getExitStatus().equals(ExitStatus.COMPLETED))
            {
                Files.delete(Paths.get(TEMP_STORAGE + originalFileName));
            }
        } catch (JobExecutionAlreadyRunningException | JobParametersInvalidException |
                 JobInstanceAlreadyCompleteException | JobRestartException e) {
            throw new RuntimeException(e);
        }
    }
}
