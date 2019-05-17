package happy.min.jee;

import java.util.HashMap;

// 4^^번


import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.ServletConfigAware;



//4. dispatcher에 의해 연결된 Class @Controller 생성
@Controller
public class Moon2seulCtrl implements ServletConfigAware {
	
	/**
	 * 채팅에 관련된 정보를 담기 위해 Application 객체 생성
	 */
	private ServletContext servletContext;
	
	public String createUUID(){
		return UUID.randomUUID().toString();
	}
	
	//8. log처리를 위한 logger객체 생성
	Logger logger = LoggerFactory.getLogger(Moon2seulCtrl.class);
	
	
	@Override
	public void setServletConfig(ServletConfig servletConfig) {
		servletContext = servletConfig.getServletContext();
	}
	
	
	// WebSocket 채팅 접속했을 때
	@RequestMapping(value="/socketOpen.do" , method=RequestMethod.GET)
	public String socketOpen(HttpSession session, Model model, String mem, String gr){
		
		session.setAttribute("mem_id", mem);
		session.setAttribute("gr_id", gr);
		
		
		logger.info("socketOpen 소켓 화면 이동 1)리스트에 접속자 값 넣기");
		String mem_id = (String)session.getAttribute("mem_id");
		String gr_id = (String)session.getAttribute("gr_id");
		logger.info(mem_id +"::"+gr_id);
		HashMap<String, String> chatList = (HashMap<String, String>)servletContext.getAttribute("chatList");
		if(chatList == null){
			chatList = new HashMap<String, String>();
			chatList.put(mem_id, gr_id);
			servletContext.setAttribute("chatList", chatList);
		}else{
			chatList.put(mem_id, gr_id);
			servletContext.setAttribute("chatList", chatList);
		}
		logger.info("socketOpen 소켓 화면 이동 2)리스트 값 전달");
		
		return "groupChat";
	}
	
	//WebSocket 채팅 종료했을 때
	@RequestMapping(value = "/socketOut.do", method={RequestMethod.GET, RequestMethod.POST})
	public void socketOut(HttpSession session){
		logger.info("socketOut 소켓에서 나오기");
		String mem_id = (String)session.getAttribute("mem_id");
		HashMap<String, String> chatList = (HashMap<String, String>)servletContext.getAttribute("chatList");
		System.out.println("기존 접속 회원 리스트:"+chatList);
		if(chatList != null){
			chatList.remove(mem_id);
		}
		System.out.println("갱신 후 접속 회원 리스트:"+chatList);
		servletContext.setAttribute("chatList", chatList);
		
	}
	
	//채팅 접속자 리스트 출력
	@RequestMapping(value = "/viewChatList.do", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Map<String, String>> viewChatList(){
		Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
		Map<String, String> chatList = (HashMap<String, String>)servletContext.getAttribute("chatList");
		map.put("list", chatList);
		return map;
	}
	
	
}








