package com.mycompany.webapp.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mycompany.webapp.dto.Board;
import com.mycompany.webapp.dto.Pager;
import com.mycompany.webapp.service.BoardService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/board")
@Slf4j
public class BoardController {
	@RequestMapping("/test")
	public Board test() {
		log.info("실행");
		Board board = new Board();
		board.setBno(1);
		board.setBtitle("제목");
		board.setBcontent("내용");
		board.setMid("user");
		board.setBdate(new Date());
		return board;
	}
	
	@Resource
	private BoardService boardService;
	
	@GetMapping("/list")
	public Map<String, Object> list(@RequestParam(defaultValue="1") int pageNo) {
		log.info("실행");
		int totalRows = boardService.getTotalBoardNum();
		Pager pager = new Pager(5, 5, totalRows, pageNo);
		List<Board> list = boardService.getBoards(pager);
		Map<String, Object> map = new HashMap<>();
		map.put("boards", list);
		map.put("pager", pager);
		return map;
	}
	
	@GetMapping("/{bno}")
	public Board read(@PathVariable int bno, @RequestParam(defaultValue = "false") boolean hit) {
		log.info("실행");
		Board board = boardService.getBoard(bno, hit);
		return board;
	}
	
	@PostMapping("/create")
	public Board create(Board board) {
		log.info("실행");
		if(board.getBattach() != null && !board.getBattach().isEmpty()) {
			MultipartFile mf = board.getBattach();
			board.setBattachoname(mf.getOriginalFilename());
			board.setBattachsname(new Date().getTime() + "-" + mf.getOriginalFilename());
			board.setBattachtype(mf.getContentType());
			try {
			File file = new File("C:/hyndai_it&e/upload_files/" + board.getBattachsname());
			mf.transferTo(file);
			} catch(Exception e) {}
			// 실행이 됐다면 battachoname이 있기 때문에 board.xml에서 battachoname != null 부분 실행. (동적쿼리)
		}
		boardService.writeBoard(board); // 이 이후엔 파라미터에 bno가 들어가 있음
		board = boardService.getBoard(board.getBno(), false); // false로 주는 이유 사용자가 보고 싶어서 게시물 보기를 누른 것이 아닌 응답으로 제공할 목적이기 때문. 조회수 안올리기.
		//boardService의 getBoard 참고
		return board;
	}
	
	//Multipart/form-data로 데이터를 전송하고 수정한다면 put이나 patch는 못쓰고 post방식만 사용가능
	//Mutlipart/form-data로 데이터를 전송하고 수정 안한다면 put이나 patch mapping 사용해야 함.
	@PostMapping("/update")
	public Board update(Board board) {
		log.info("실행");
		if(board.getBattach() != null && !board.getBattach().isEmpty()) {
			MultipartFile mf = board.getBattach();
			board.setBattachoname(mf.getOriginalFilename());
			board.setBattachsname(new Date().getTime() + "-" + mf.getOriginalFilename());
			board.setBattachtype(mf.getContentType());
			try {
				File file = new File("C:/hyndai_it&e/upload_files/" + board.getBattachsname());
				mf.transferTo(file);
			} catch(Exception e) {}
			// 실행이 됐다면 battachoname이 있기 때문에 board.xml에서 battachoname != null 부분 실행. (동적쿼리)
		}
		boardService.updateBoard(board); // 이 이후엔 파라미터에 bno가 들어가 있음
		board = boardService.getBoard(board.getBno(), false); // false로 주는 이유 사용자가 보고 싶어서 게시물 보기를 누른 것이 아닌 응답으로 제공할 목적이기 때문. 조회수 안올리기.
		//boardService의 getBoard 참고
		return board;
	}
	
	@DeleteMapping("/{bno}")
	public Map<String, String> delete(@PathVariable int bno) {
		log.info("실행");
		boardService.removeBoard(bno);
		Map<String, String> map = new HashMap<>();
		map.put("result", "success");
		return map;
	}
	
	// void 말고 byte[]도 되나 사진 파일이 10mb면 10mb의 배열이 만들어져서 좋지 않음.
	@GetMapping("/battach/{bno}")
	public void download(@PathVariable int bno, HttpServletResponse response) {
		try {
			Board board = boardService.getBoard(bno, false);
			String battachoname = board.getBattachoname();
			if(battachoname == null) return;
			
			//파일 이름이 한글로 되어 있을 경우, 응답 헤더에 한글을 넣을 수 있기 때문에 변환해야 함.
			battachoname = new String(battachoname.getBytes("UTF-8"), "ISO-8859-1"); // UTF-8로 인코딩하고 다시 ISO-8859-1로 변경
			String battachsname = board.getBattachsname();
			String battachtype = board.getBattachtype();
			
			//응답 생성
			//Content-Disposition: attachment; filename="a.jpg";
			response.setHeader("Content-Disposition", "attachment; filename=\"" + battachoname + "\";");
			response.setContentType(battachtype);
			
			InputStream is = new FileInputStream("C:/hyndai_it&e/upload_files/" + battachsname);
			OutputStream os = response.getOutputStream();
			FileCopyUtils.copy(is, os);
			is.close();
			os.flush();
			os.close();
		}catch(Exception e) {
			
		}
	}
}
