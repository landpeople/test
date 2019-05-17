package happy.min.jee.socket;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.context.ServletConfigAware;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;


@Component(value="wsChat.do")
public class MySocketHandler extends TextWebSocketHandler{

	Logger logger = LoggerFactory.getLogger(MySocketHandler.class);
	
	private ArrayList<WebSocketSession> list ; //webSocket session값을 담은 리스트

	public MySocketHandler() {
		list = new ArrayList<WebSocketSession>();
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		logger.info("afterConnectionEstablished()실행");
		super.afterConnectionEstablished(session);

		list.add(session);	//전체 접속자 리스트에 새로운 접속자 추가
		System.out.println("client session cnt : "+list.size()); 
		System.out.println("session connected : "+session.getId());
	}

	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		logger.info("handleTextMessage()실행");
		String msg = message.getPayload();
		String txt = "";

		Map<String, Object> mySession = session.getHandshakeAttributes();	//WebsocketSession의 session값을 httpSesssion값으로 변경
		String myGrSession = (String)mySession.get("gr_id");	//접속자의 그룹 아이디
		String myMemSession = (String)mySession.get("mem_id");	//접속자 아이디
		System.err.println(myGrSession+":"+myMemSession+"----------------");

		if( msg != null && !msg.equals("") ) {
			if(msg.indexOf("#$nick_") > -1 ) {

				for(WebSocketSession s : list) {	
					Map<String, Object> sessionMap = s.getHandshakeAttributes();
					String otherGrSession = (String)sessionMap.get("gr_id");
					String otherMemSession = (String)sessionMap.get("mem_id");

					ArrayList<String> grMemList = new ArrayList<String>();
					System.out.println("그룹아이디: "+myGrSession);
					System.out.println("멤버아이디2: "+otherMemSession);

					if(myGrSession.equals(otherGrSession)) {	//같은 그룹 소속일 때
						s.sendMessage(
								new TextMessage("<font color='red' size='1px'>"+myMemSession+" 님이 입장했습니다.</font>")
								);
					}
				}
			}else {
				String msg2 = msg.substring(0, msg.indexOf(":")).trim();
				for(WebSocketSession s : list) {
					Map<String, Object> sessionMap = s.getHandshakeAttributes();
					String otherGrSession = (String)sessionMap.get("gr_id");
					String otherMemSession = (String)sessionMap.get("mem_id");
					if(myGrSession.equals(otherGrSession)){
						if(msg2.equals(otherMemSession)){
							String newMsg = "[나]"+msg.replace(msg.substring(0, msg.trim().indexOf(":")+1),"");
							System.out.println("newMsg:"+newMsg);
							txt = newMsg;
						}else{
							String part1 = msg.substring(0, msg.trim().indexOf(":")).trim();
							String part2 = "["+part1+"] \n"+msg.substring(msg.trim().indexOf(":")+1);
							txt = part2;
						}
						s.sendMessage(new TextMessage(txt));
					}
				}
			}
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session,CloseStatus status) throws Exception {
		logger.info("afterConnectionClosed()실행");
		
		super.afterConnectionClosed(session, status);
		Map<String, Object> mySession = session.getHandshakeAttributes();
		String myGrSession = (String)mySession.get("gr_id");
		String myMemSession = (String)mySession.get("mem_id");
		list.remove(session);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분");
		String now = sdf.format(new Date());
		for(WebSocketSession a : list) {
			Map<String, Object> sessionMap = a.getHandshakeAttributes();
			String otherGrSession = (String)sessionMap.get("gr_id");
			if(myGrSession.equals(otherGrSession)){
				a.sendMessage(new TextMessage("<font color='blue' size='1px'>"+myMemSession+"님이 퇴장했습니다 ("+now+")</font>"));
			}
		}
	}
}
