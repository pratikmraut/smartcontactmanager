package com.contact.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.contact.dao.ContactRepository;
import com.contact.dao.UserRepository;
import com.contact.entities.Contact;
import com.contact.entities.User;
import com.contact.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;

	// method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName = principal.getName();
		System.out.println("userName " + userName);

		User user = userRepository.getUserByUserName(userName);

		System.out.println("user" + user);

		model.addAttribute("user", user);
	}
	
	// Check and increment the count, remove message if displayed
	private void checkAndRemoveMessage(HttpSession session) {
		
	    Message message = (Message) session.getAttribute("message");
	    if (message != null) {
		    System.out.println("@#message- " + message);
		    System.out.println("@#message.getCount()- " + message.getCount());
	        if (message.getCount() > 0) {
	            session.removeAttribute("message");
	        } else {
	            message.incrementCount();
	        }
	    }
	}


	// dashboard home
	@GetMapping("/index")
	public String dashboard(Model model, Principal principal, HttpSession session) {

		model.addAttribute("title", "User Dashboard");
		
		// clear the previous session message
        checkAndRemoveMessage(session);
        
		return "normal/user_dashboard";
	}

	// open add form handler
	@GetMapping("/addcontact")
	public String openAddContactForm(Model model, HttpSession session) {

		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());

		// clear the previous session message
        checkAndRemoveMessage(session);

		/*Message message = (Message) session.getAttribute("message");
		if (message != null) {

			if ("Contact deleted successfully".equals(message.getContent())
					|| "Contact updated successfully".equals(message.getContent())) {
				System.out.println("Message: " + message.getContent());
				System.out.println("Type: " + message.getType());
				session.removeAttribute("message");
			}
		}*/

		return "normal/add_contact_form";
	}

	// processing and contact form
	@PostMapping("/processcontact")
	public String processContact(@Valid @ModelAttribute Contact contact, BindingResult result,
			@RequestParam("image") MultipartFile file, Principal principal, Model model, HttpSession session) {

		if (result.hasErrors()) {
			System.out.println("ERROR " + result.toString());
			System.out.println("ERROR " + contact);

			if (contact.getcId() == 0) {
				System.out.println("ERROR " + contact);

				model.addAttribute("contact", contact);
				session.setAttribute("message", new Message("Something went wrong", "alert-danger"));
				return "normal/add_contact_form";
			}

			return "normal/add_contact_form";
		}

		System.out.println("contact details-" + contact.toString());

		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);

			contact.setUser(user);
			user.getContacts().add(contact);

			// Processing and uploading file

			if (file.isEmpty()) {

				System.out.println("File(Image) is empty");
				contact.setImageUrl("d-photo.png");

			} else {

				// Upload the file to folder and update the name to contact
				contact.setImageUrl(file.getOriginalFilename());

				File saveFile = new ClassPathResource("static/image").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				System.out.println("Image is uploaded");
			}

			this.userRepository.save(user);

			System.out.println("data- " + contact);
			System.out.println("Added to database - " + user);

			// success message
			session.setAttribute("message", new Message("Your contact is added !! Add more..", "alert-success"));

			model.addAttribute("contact", new Contact());

			return "redirect:/user/addcontact";

		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
			// error message
			session.setAttribute("message", new Message("Something went wrong", "alert-danger"));
			return "normal/add_contact_form";
		}

	}

	// show contact handler
	// per page = 5[n]
	// current page = 0 [page]

	@GetMapping("/showcontact/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model model, Principal principal,
			HttpSession session) {

		model.addAttribute("title", "Show Contact");

		// clear the previous session message
        checkAndRemoveMessage(session);

		/*Message message = (Message) session.getAttribute("message");
		if (message != null) {
			if ("Contact updated successfully".equals(message.getContent()) 
					|| "Your contact is added !! Add more..".equals(message.getContent())) {
				System.out.println("Message: " + message.getContent());
				System.out.println("Type: " + message.getType());
				session.removeAttribute("message");
			}
		}*/

		// contact list from database

		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);

		// currentPage - page
		// contact per page - 12
		PageRequest pegable = PageRequest.of(page, 12);

		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pegable);

		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());

		return "normal/showcontact";
	}

	// showing perticular contact details

	@GetMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId, Model model, Principal principal,
			HttpSession session) {
		System.out.println("cId -  " + cId);
		model.addAttribute("title", "Contact Detail");
		

		// clear the previous session message
        checkAndRemoveMessage(session);

		/*Message message = (Message) session.getAttribute("message");
		if (message != null) {
			if ("Contact deleted successfully".equals(message.getContent())
					|| "Your contact is added !! Add more..".equals(message.getContent())) {
				System.out.println("Message: " + message.getContent());
				System.out.println("Type: " + message.getType());
				session.removeAttribute("message");
			}
		}*/

		Optional<Contact> contactOptional = this.contactRepository.findById(cId);

		Contact contact = contactOptional.get();

		// getting logged in user
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);

		if (user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
		}

		return "normal/contact_detail";
	}

	// delete contact handler
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId, Model model, Principal principal,
			HttpSession session) {

		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();

		// getting logged in user
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);

		if (user.getId() == contact.getUser().getId()) {

			// remove photo

			System.out.println("contact.getImageUrl(); = " + contact.getImageUrl());

			contact.setUser(null);

			this.contactRepository.delete(contact);

			System.out.println("##DELETED##");
			session.setAttribute("message", new Message("Contact deleted successfully", "alert-success"));
			return "redirect:/user/showcontact/0";
		}
		return "normal/showcontact";
	}

	// open update form handler
	@PostMapping("/updatecontact/{cId}")
	public String updateForm(@PathVariable("cId") Integer cId, Model model, HttpSession session) {
		
		// clear the previous session message
		checkAndRemoveMessage(session);

		model.addAttribute("title", "Update Contact");

		Contact contact = this.contactRepository.findById(cId).get();

		model.addAttribute("contact", contact);

		return "normal/update_form";
	}

	// update contact handler
	@PostMapping("/processupdate")
	public String updateContactHandler(@Valid @ModelAttribute Contact contact, BindingResult result,
			@RequestParam("image") MultipartFile file, Principal principal, Model model, HttpSession session) {


		// clear the previous session message
        checkAndRemoveMessage(session);

		/*Message message = (Message) session.getAttribute("message");
		if (message != null) {
			if ("Contact deleted successfully".equals(message.getContent())
					|| "Your contact is added !! Add more..".equals(message.getContent())) {
				System.out.println("Message: " + message.getContent());
				System.out.println("Type: " + message.getType());
				session.removeAttribute("message");
			}
		}*/

		System.out.println("Contact details - " + contact);
		System.out.println("Contact name - " + contact.getName());
		System.out.println("Contact ID - " + contact.getcId());

		try {

			// old contact details
			Contact oldContact = this.contactRepository.findById(contact.getcId()).get();

			if (file.isEmpty()) {
				System.out.println("File(Image) is empty");
				contact.setImageUrl(oldContact.getImageUrl());

			} else {

				// delete old photo
				File deleteFile = new ClassPathResource("static/image").getFile();
				File file1 = new File(deleteFile, oldContact.getImageUrl());
				file1.delete();

				// upload new photo
				File saveFile = new ClassPathResource("static/image").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				contact.setImageUrl(file.getOriginalFilename());

			}

			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			session.setAttribute("message", new Message("Contact updated successfully", "alert-success"));

			return "redirect:/user/" + contact.getcId() + "/contact";

		} catch (Exception e) {
			e.printStackTrace();
		}

		return "normal/update_form";
	}

	// your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model, HttpSession session) {
		
		model.addAttribute("title", "Profile Page");
		
		// clear the previous session message
		checkAndRemoveMessage(session);

		/*Message message = (Message) session.getAttribute("message");
		if (message != null) {
			System.out.println("Message: " + message.getContent());
			System.out.println("Type: " + message.getType());
			session.removeAttribute("message");
		}*/

		return "normal/profile";
	}

}
