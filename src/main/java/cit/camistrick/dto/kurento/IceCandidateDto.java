package cit.camistrick.dto.kurento;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IceCandidateDto {
    @Getter
    public static class Request extends KurentoDto {
        private String id;
        private String name;
        private Candidate candidate;
    }

    public static class Response {
    }

    @Getter
    public static class Candidate {
        private String candidate;
        private String sdpMid;
        private int sdpMLineIndex;
    }
}
