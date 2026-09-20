package com.newlegacyjam.platform.api;

import com.newlegacyjam.platform.api.NljModels.VoteRequest;
import com.newlegacyjam.platform.api.NljModels.VoteResponse;
import com.newlegacyjam.platform.voting.VotingFacade;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class VoteController {
    private final VotingFacade voting;

    public VoteController(VotingFacade voting) {
        this.voting = voting;
    }

    @PostMapping("/votes")
    public ResponseEntity<VoteResponse> vote(
            @RequestHeader(name = "X-Line-ID-Token", required = false) String token,
            @Valid @RequestBody VoteRequest request) {
        return ResponseEntity.status(201).body(voting.vote(token, request));
    }
}
