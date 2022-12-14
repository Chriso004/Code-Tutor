package com.codeTutor.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.codeTutor.Service.ContentService;
import com.codeTutor.Service.KeyWordService;
import com.codeTutor.model.Content;
import com.codeTutor.model.KeyWord;
import com.codeTutor.model.User;

@Controller
public class ContentController {
	@Autowired
	private ContentService contentdb;
	@Autowired
	private KeyWordService keyworddb;

	@GetMapping("/content/getAllContent.do")
	public String getAllContents(Model model) {
		List<Content> content = contentdb.selectAll();
		model.addAttribute("allContent", content);
		System.out.println(content);

		return "redirect:/";
	}

	@GetMapping("/content/getContent.do")
	public String getContentByFid(Model model, @RequestParam(value = "fid", required = true) int fid) {
		Content content = contentdb.selectByFid(fid);
		model.addAttribute("Content", content);

		return "contentView";
	}

	@PostMapping("/content/postContent.do")
	public String postContent(Model model, @ModelAttribute Content c, @ModelAttribute KeyWord k,
			@SessionAttribute(name = "loginUser", required = false) User user) {
		
		if(user == null) {
			model.addAttribute("msg", "잘못된 접근입니다.");
			model.addAttribute("url", "/");
			
			return "alert";
		}
		
		c.setDate();
		c.setAuthor(user.getNickname());

		boolean contentResponse = contentdb.insertContent(c);
		if (contentResponse) {
			int fid = Integer.parseInt(contentdb.getLastID());
			k.setFid(fid);
			boolean keywordResponse = keyworddb.insertKeyWord(k);

			if (keywordResponse) {
				model.addAttribute("msg", "성공적으로 추가되었습니다.");
				model.addAttribute("url", "/");
				return "alert";
			}
		}

		model.addAttribute("msg", "오류가 발생하였습니다.");
		model.addAttribute("url", "/post");

		return "alert";
	}

	@PostMapping("/content/updateContent.do")
	public String updateContent(Model model, @ModelAttribute Content c, @ModelAttribute KeyWord k,
			@SessionAttribute(name = "loginUser", required = false) User user) {
		if(user == null) {
			model.addAttribute("msg", "잘못된 접근입니다.");
			model.addAttribute("url", "/");
			
			return "alert";
		}
		
		c.setDate();
		c.setAuthor(user.getNickname());
		contentdb.updateContent(c);
		keyworddb.updateKeyWord(k);

		model.addAttribute("msg", "수정이 완료되었습니다.");
		model.addAttribute("url", "/contentView?fid=" + c.getFid());

		return "alert";
	}

	@PostMapping("/content/deleteContent.do")
	public String deleteContent(Model model, @RequestParam(value = "fid", required = true) int fid,
			@SessionAttribute(name = "loginUser", required = false) User user) {
		if(user == null || !userValidate(fid, user.getNickname())) {
			model.addAttribute("msg", "잘못된 접근입니다.");
			model.addAttribute("url", "/");
			System.out.println(userValidate(fid, user.getNickname()));
			return "alert";
		}
		keyworddb.deleteKeyWord(fid);
		contentdb.deleteContent(fid);

		model.addAttribute("msg", "삭제가 완료되었습니다.");
		model.addAttribute("url", "/");

		return "alert";
	}
	
	private boolean userValidate(int fid, String userName) {
		String author = contentdb.selectByFid(fid).getAuthor();
		return author.equals(userName);
	}
}
