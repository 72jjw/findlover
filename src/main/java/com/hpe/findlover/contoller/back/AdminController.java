package com.hpe.findlover.contoller.back;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("admin")
public class AdminController {
	@GetMapping
	public String index(){
		return "back/index";
	}
	@GetMapping("users/basic")
	public String userBasicList(){
		return "back/user/user_basic_list";
	}
	@GetMapping("left")
	public String left(){
		return "back/common/left";
	}
}