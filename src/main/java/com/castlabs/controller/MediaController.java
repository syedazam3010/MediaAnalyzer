package com.castlabs.controller;

import java.io.IOException;
import java.util.List;

import com.castlabs.model.Box;
import com.castlabs.service.MediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.castlabs.model.Url;

@RestController
public class MediaController {
	@Autowired
	private MediaService service;

	@PostMapping(value = "/analyze")
	public ResponseEntity<List<Box>> getMediaResult(@RequestBody Url myurl) throws IOException{

		try {
			List<Box> resp = service.getMediaResult(myurl.getUrl());
			return ResponseEntity.ok(resp);
		} catch (Exception e) {
			// Handle any errors or exceptions
			e.printStackTrace();
			return ResponseEntity.ok(null);
		}


	}
	
}
