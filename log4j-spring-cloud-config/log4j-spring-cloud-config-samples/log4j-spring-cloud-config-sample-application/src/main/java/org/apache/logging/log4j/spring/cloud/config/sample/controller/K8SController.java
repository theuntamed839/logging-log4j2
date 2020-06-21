/*
 * Copyright (c) 2020 Nextiva, Inc. to Present.
 * All rights reserved.
 */

package org.apache.logging.log4j.spring.cloud.config.sample.controller;

import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.kubernetes.KubernetesClientBuilder;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;

/**
 * Test class
 */
@RestController
public class K8SController {

    private static final Logger LOGGER = LogManager.getLogger(K8SController.class);
    private static final String HOSTNAME = "HOSTNAME";
    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/k8s/pod")
    public ResponseEntity<Pod> getPod() {
        try {
            KubernetesClient client = new KubernetesClientBuilder().createClient();
            if (client != null) {
                Pod pod = getCurrentPod(client);
                if (pod != null) {
                    LOGGER.info("Pod: {}", objectMapper.writeValueAsString(pod));
                    return new ResponseEntity<>(pod, HttpStatus.OK);
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Unable to obtain or print Pod information", ex);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Pod getCurrentPod(KubernetesClient kubernetesClient) {
        String hostName = System.getenv(HOSTNAME);
        try {
            if (isServiceAccount() && Strings.isNotBlank(hostName)) {
                return kubernetesClient.pods().withName(hostName).get();
            }
        } catch (Throwable t) {
            LOGGER.debug("Unable to locate pod with name {}.", hostName);
        }
        return null;
    }

    private boolean isServiceAccount() {
        return Paths.get(Config.KUBERNETES_SERVICE_ACCOUNT_TOKEN_PATH).toFile().exists()
                && Paths.get(Config.KUBERNETES_SERVICE_ACCOUNT_CA_CRT_PATH).toFile().exists();
    }

}
