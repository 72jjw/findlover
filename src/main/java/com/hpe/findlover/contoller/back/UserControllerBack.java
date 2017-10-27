package com.hpe.findlover.contoller.back;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hpe.findlover.model.Label;
import com.hpe.findlover.model.UserAsset;
import com.hpe.findlover.model.UserBasic;
import com.hpe.findlover.model.UserDetail;
import com.hpe.findlover.service.BaseService;
import com.hpe.findlover.service.*;
import com.hpe.findlover.util.LoverUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.List;

/**
 * @author Gss
 */
@Controller
@RequestMapping("admin/user")
public class UserControllerBack {
	private Logger logger = LogManager.getLogger(UserControllerBack.class);
	private final UserService userBasicService;
	private final UserAssetService userAssetService;
	private final UserDetailService userDetailService;
	private final UserLifeService userLifeService;
	private final UserStatusService userStatusService;
	private final UserPickService userPickService;
	private final LabelService labelService;

	@Autowired
	public UserControllerBack(UserService userBasicService, UserAssetService userAssetService, UserDetailService userDetailService, UserLifeService userLifeService, UserStatusService userStatusService, UserPickService userPickService, LabelService labelService) {
		this.userBasicService = userBasicService;
		this.userAssetService = userAssetService;
		this.userDetailService = userDetailService;
		this.userLifeService = userLifeService;
		this.userStatusService = userStatusService;
		this.userPickService = userPickService;
		this.labelService = labelService;
	}

	@GetMapping("basic")
	@ResponseBody
	public PageInfo<UserBasic> userBasicList(Page<UserBasic> page, @RequestParam String identity, @RequestParam String column, @RequestParam String keyword) {
		logger.info("接收参数：identity=" + identity + ",pageNum=" + page.getPageNum() + ",pageSize=" + page.getPageSize() + ",column=" + column + ",keyword=" + keyword);
		PageHelper.startPage(page.getPageNum(), page.getPageSize());
		List<UserBasic> basics = userBasicService.selectAllByIdentity(identity, column, "%" + keyword + "%");
		// 遍历list查出所有相对应的Asset和Detail数据
		basics.forEach(user -> {
			UserAsset asset = userAssetService.selectByPrimaryKey(user.getId());
			UserDetail detail = userDetailService.selectByPrimaryKey(user.getId());
			if (asset != null) {
				user.setVip(LoverUtil.getDiffOfHours(asset.getVipDeadline()) > 0);
				user.setStar(LoverUtil.getDiffOfHours(asset.getStarDeadline()) > 0);
			}
			if (detail != null) {
				user.setAuthenticated(detail.getCardnumber() != null);
			}
		});
		PageInfo<UserBasic> pageInfo = new PageInfo<>(basics);
		logger.info(JSONObject.toJSON(pageInfo));
		return pageInfo;
	}

	@GetMapping("{type}/{id}")
	@ResponseBody
	@Cacheable(value = "user-cache")
	public Object userBasic(@PathVariable int id, @PathVariable String type) throws NoSuchFieldException, IllegalAccessException {
		logger.debug("获取ID为" + id + "的User" + StringUtils.capitalize(type) + "数据...");
		Field declaredField = this.getClass().getDeclaredField("user" + StringUtils.capitalize(type) + "Service");
		declaredField.setAccessible(true);
		return ((BaseService) declaredField.get(this)).selectByPrimaryKey(id);
	}

	@GetMapping("details/{id}")
	public String userDetail(@ModelAttribute @PathVariable int id) {
		return "back/user/user_detail";
	}

	@GetMapping("detail")
	public String userDetailPre() {
		return "back/user/user_detail_pre";
	}

	@GetMapping("list")
	public String userList() {
		return "back/user/user_list";
	}


	@GetMapping("label")
	public String labelList(Model model) {
		model.addAttribute("labels", labelService.selectAll());
		return "back/user/label";
	}

	@PutMapping("basic/{id}")
	@ResponseBody
	public boolean updateUser(@PathVariable int id, UserBasic userBasic) {
		userBasic.setId(id);
		return userBasicService.updateByPrimaryKeySelective(userBasic);
	}

	@PostMapping("label")
	@ResponseBody
	public int addLabel(Label label) {
		if (labelService.insertUseGeneratedKeys(label) > 0) {
			return label.getId();
		} else {
			return 0;
		}
	}

	@PostMapping("label/exists")
	@ResponseBody
	public boolean selectLabel(@RequestParam String name) {
		Label label = new Label();
		label.setName(name);
		boolean result = labelService.selectOne(label) != null;
		logger.debug("名称为“" + name + "”的标签是否存在：" + result);
		return result;
	}

	@DeleteMapping("label/{id}")
	@ResponseBody
	public boolean deleteLabel(@PathVariable int id) {
		return labelService.deleteByPrimaryKey(id) > 0;
	}

	@PutMapping("label/{id}")
	@ResponseBody
	public boolean updateLabel(@PathVariable int id, Label label) {
		label.setId(id);
		return labelService.updateByPrimaryKey(label);
	}
}
