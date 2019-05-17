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
	
	private ArrayList<WebSocketSession> list ; //webSocket session���� ���� ����Ʈ

	public MySocketHandler() {
		list = new ArrayList<WebSocketSession>();
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		logger.info("afterConnectionEstablished()����");
		super.afterConnectionEstablished(session);

		list.add(session);	//��ü ������ ����Ʈ�� ���ο� ������ �߰�
		System.out.println("client session cnt : "+list.size()); 
		System.out.println("session connected : "+session.getId());
	}

	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		logger.info("handleTextMessage()����");
		String msg = message.getPayload();
		String txt = "";

		Map<String, Object> mySession = session.getHandshakeAttributes();	//WebsocketSession�� session���� httpSesssion������ ����
		String myGrSession = (String)mySession.get("gr_id");	//�������� �׷� ���̵�
		String myMemSession = (String)mySession.get("mem_id");	//������ ���̵�
		System.err.println(myGrSession+":"+myMemSession+"----------------");

		if( msg != null && !msg.equals("") ) {
			if(msg.indexOf("#$nick_") > -1 ) {

				for(WebSocketSession s : list) {	
					Map<String, Object> sessionMap = s.getHandshakeAttributes();
					String otherGrSession = (String)sessionMap.get("gr_id");
					String otherMemSession = (String)sessionMap.get("mem_id");

					ArrayList<String> grMemList = new ArrayList<String>();
					System.out.println("�׷���̵�: "+myGrSession);
					System.out.println("������̵�2: "+otherMemSession);

					if(myGrSession.equals(otherGrSession)) {	//���� �׷� �Ҽ��� ��
						s.sendMessage(
								new TextMessage("<font color='red' size='1px'>"+myMemSession+" ���� �����߽��ϴ�.</font>")
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
							String newMsg = "[��]"+msg.replace(msg.substring(0, msg.trim().indexOf(":")+1),"");
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
		logger.info("afterConnectionClosed()����");
		
		super.afterConnectionClosed(session, status);
		Map<String, Object> mySession = session.getHandshakeAttributes();
		String myGrSession = (String)mySession.get("gr_id");
		String myMemSession = (String)mySession.get("mem_id");
		list.remove(session);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy�� MM�� dd�� HH�� mm��");
		String now = sdf.format(new Date());
		for(WebSocketSession a : list) {
			Map<String, Object> sessionMap = a.getHandshakeAttributes();
			String otherGrSession = (String)sessionMap.get("gr_id");
			if(myGrSession.equals(otherGrSession)){
				a.sendMessage(new TextMessage("<font color='blue' size='1px'>"+myMemSession+"���� �����߽��ϴ� ("+now+")</font>"));
			}
		}
	}
}
