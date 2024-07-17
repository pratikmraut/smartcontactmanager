package com.contact.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.contact.dao.UserRepository;
import com.contact.entities.User;
import com.contact.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;

	@GetMapping("/")
	public String home(Model m) {

		m.addAttribute("title", "Home - Smart Contact Manager");
		return "home";
	}

	@GetMapping("/about")
	public String about(Model m) {

		m.addAttribute("title", "About - Smart Contact Manager");
		return "about";
	}

	@GetMapping("/signup")
	public String signup(Model m) {

		m.addAttribute("title", "Register - Smart Contact Manager");
		m.addAttribute("user", new User());
		return "signup";
	}

	/*
	 * public String loginFail(Model model) { model.addAttribute("error", true);
	 * return "signin"; }
	 */

	// handler for custom login
	@GetMapping("/signin")
	public String customLogin(Model m) {

		m.addAttribute("title", "Login Page");
		m.addAttribute("user", new User());
		return "login";
	}

	// handler for registering the user
	@PostMapping("/doregister")
	public String register(@Valid @ModelAttribute("user") User user, BindingResult result,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model m,
			HttpSession session) {

		if (result.hasErrors()) {
			System.out.println("ERROR" + result.toString());
			m.addAttribute("user", user);
			session.setAttribute("message", new Message("Please fill all the fields correctly.", "alert-danger"));
			return "signup";
		}

		if (!agreement) {
			System.out.println("agreement " + agreement);
			session.setAttribute("message",
					new Message("You have not agreed to the terms and conditions.", "alert-danger"));
			m.addAttribute("user", user); // Retain user input
			return "signup";
		}

		try {

			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));

			System.out.println("agreement " + agreement);
			System.out.println("user " + user);

			User res = this.userRepository.save(user);

			m.addAttribute("user", new User());

			if (res != null) {
				session.setAttribute("message", new Message("Successfully Registered !! ", "alert-success"));
				return "redirect:/signup";
			} else {
				session.setAttribute("message", new Message("Registration failed. Please try again.", "alert-danger"));
				m.addAttribute("user", user);
				return "signup";
			}

		} catch (DataIntegrityViolationException e) {

			if (e.getRootCause() instanceof java.sql.SQLIntegrityConstraintViolationException) {
				session.setAttribute("message",
						new Message("Email already registered! Please use a different email.", "alert-danger"));
			} else {
				session.setAttribute("message",
						new Message("Something went wrong !! " + e.getMessage(), "alert-danger"));
			}
			m.addAttribute("user", user);
			return "signup";
		} catch (Exception e) {

			e.printStackTrace();
			m.addAttribute("user", user);
			session.setAttribute("message", new Message("Something went wrong !! " + e.getMessage(), "alert-danger"));
			return "signup";
		}
	}

}

/*
 * @GetMapping("/test")
 * 
 * @ResponseBody public String test() {
 * 
 * System.out.println("in conr");
 * 
 * User user = new User();
 * 
 * user.setName("Pratik"); user.setEmail("pratik123@gmail.com");
 * 
 * Contact contact = new Contact(); user.getContacts().add(contact);
 * 
 * userRepository.save(user);
 * 
 * System.out.println("out conr");
 * 
 * return "Working"; }
 */
