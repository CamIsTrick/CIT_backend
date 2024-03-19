package cit.camistrick.action;

import cit.camistrick.domain.UserSession;
import cit.camistrick.service.UserSessionService;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.IceCandidate;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

import static cit.camistrick.dto.kurento.IceCandidateDto.Candidate;
import static cit.camistrick.dto.kurento.IceCandidateDto.Request;

@Slf4j
@RequiredArgsConstructor
public class IceCandidateAction implements KurentoAction {
    private final UserSessionService userSessionService;

    @Override
    public void process(WebSocketSession session, JsonObject jsonMessage) throws IOException {
        Request requestDto = new Request().toDto(jsonMessage, Request.class);
        UserSession userSession = userSessionService.findSession(session.getId());
        IceCandidate iceCandidate = makeIceCandidate(requestDto.getCandidate());
        /*
        username이 중복 가능이기때문에 현재는 session Id를 통해서 outgoing과 incoming을 구분
         */
        /*

         */
        userSession.addCandidate(iceCandidate, userSession.getSessionId());
    }

    @Override
    public void onError() {
        log.error("IceCandidateAction : Error Occurred");
    }

    private IceCandidate makeIceCandidate(Candidate candidateDto) {
        return new IceCandidate(
                candidateDto.getCandidate(),
                candidateDto.getSdpMid(),
                candidateDto.getSdpMLineIndex()
        );
    }
}
