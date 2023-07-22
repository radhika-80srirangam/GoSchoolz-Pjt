 package com.iskool.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iskool.dto.ErrorDto;
import com.iskool.dto.OtpDTO;
import com.iskool.dto.RoleDto;
import com.iskool.dto.SendOTPDTO;
import com.iskool.dto.UserUpdateDTO;
import com.iskool.entity.LoginRequest;
import com.iskool.entity.LoginResponse;
import com.iskool.entity.ModuleMaster;
import com.iskool.entity.ModulePageMaster;
import com.iskool.entity.ModulePagePolicyMaster;
import com.iskool.entity.RoleMaster;
import com.iskool.entity.RolePagePolicyMapping;
import com.iskool.entity.User;
import com.iskool.entity.UserRole;
import com.iskool.enumeration.Status;
import com.iskool.event.PasswordSendEvent;
import com.iskool.response.Response;
import com.iskool.response.ResponseGenerator;
import com.iskool.response.TransactionContext;
import com.iskool.security.JwtTokenUtil;
import com.iskool.service.MessagePropertyService;
import com.iskool.service.ModuleMasterService;
import com.iskool.service.ModulePageMasterService;
import com.iskool.service.ModulePagePolicyMasterService;
import com.iskool.service.PageActionMasterService;
import com.iskool.service.RoleMasterService;
import com.iskool.service.RolePagePolicyMappingService;
import com.iskool.service.UserRoleService;
import com.iskool.service.UserService;
import com.iskool.util.PasswordUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.bytebuddy.utility.RandomString;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
@Api(value = "Authorization Rest API", description = "Defines endpoints that can be hit only when the user is not logged in. It's not secured by default.")
public class AuthenticationController {
	
	private static final Logger logger = Logger.getLogger(AuthenticationController.class);
	
	@Autowired
	private RolePagePolicyMappingService rolePagePolicyMappingService;
	
	@Autowired
	private PageActionMasterService pageActionMasterService;

	@Autowired
	private ModulePagePolicyMasterService modulePagePolicyMasterService;

	@Autowired
	private ModulePageMasterService modulePageMasterService;

	@Autowired
	private ModuleMasterService moduleMasterService;
	
	@Autowired
	private RoleMasterService roleMasterService;
	
	
	@Autowired
    private JwtTokenUtil jwtTokenUtil;
	
	@Autowired
    private MessageSource messageSource;
	
	@Autowired
	MessagePropertyService messagePropertySource;
	
	@Autowired
	@NonNull private ApplicationEventPublisher applicationEventPublisher;
	
	@Autowired
	private UserRoleService userRoleService;
	
	private ResponseGenerator responseGenerator;
	
	@Autowired
	public AuthenticationController(ResponseGenerator responseGenerator) {
		this.responseGenerator = responseGenerator;
	}
	
	@Autowired
	private UserService userService;
	
	@ApiOperation(value = "Logs the user in to the system and return the auth tokens")
	@RequestMapping(value = "/login", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public ResponseEntity<?> login(@ApiParam(value = "The LoginRequest payload") @RequestBody LoginRequest request, @RequestHeader HttpHeaders httpHeader) throws Exception {
		ErrorDto errorDto = null;
		Map<String, Object> response = new HashMap<String, Object>();
		if (null == request) {
			errorDto = new ErrorDto();
			errorDto.setCode("400");
			errorDto.setMessage("Invalid request rayload.!");
			response.put("status", 0);
			response.put("error", errorDto);
			return ResponseEntity.badRequest().body(response);
		}
		Optional<User> userOptional = userService.findByUserNameAndIsDeletedFalseAndIsLockedFalse(request.getUserName());
		if(!userOptional.isPresent()) {
			errorDto = new ErrorDto();
			errorDto.setCode("400");
			errorDto.setMessage("Invalid username.!");
			response.put("status", 0);
			response.put("error", errorDto);
			return ResponseEntity.badRequest().body(response);
		}
		
		User user=userOptional.get();
		String encryptedPassword = PasswordUtil.getEncryptedPassword(request.getPassword());
		
		if(!user.getPassword().equals(encryptedPassword)) {
			errorDto = new ErrorDto();
			errorDto.setCode("400");
			errorDto.setMessage("Password is wrong.!");
			response.put("status", 0);
			response.put("error", errorDto);
			return ResponseEntity.badRequest().body(response);
		}
		
		final String token = jwtTokenUtil.generateToken(user);
		response.put("status", 1);		
		response.put("message","Logged in successfully.!");
		response.put("jwt", token);
		
		List<RoleDto> roles = new ArrayList<RoleDto>();
		
		LoginResponse loginResponse = new LoginResponse();
		loginResponse.setFirstName(user.getUserName());
		loginResponse.setLastName(user.getUserName());
		loginResponse.setPhone(user.getPhoneNo());
		loginResponse.setUserId(user.getUserId());
		loginResponse.setUserName(user.getUserName());
		loginResponse.setForcePasswordChange(user.getForcePasswordChange());
		loginResponse.setUserType(user.getUserType());
		loginResponse.setRefId(user.getReferenceId());
		RoleDto roleDto =null;
		for(RoleMaster role:user.getRoles()) {
			roleDto=new RoleDto();
			roleDto.setRoleId(role.getId());
			roleDto.setRoleName(role.getRoleName());
			roles.add(roleDto);
			loginResponse.setRoleList(roles);
		}
		response.put("loginObj", loginResponse);
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		try {
			return responseGenerator.successResponse(context, response, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		/*
		logger.info(response.toString());
		return ResponseEntity.accepted().body(response);*/
	}
	
	
	/*@RequestMapping(value = "/load-meta-data", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<?> loadMetaData() throws Exception {
		
		
		Map<Integer, Country> countryMap = new HashMap<Integer, Country>();
		Map<Integer, List<State>> stateMap = new HashMap<Integer, List<State>>();
		Connection connection = DatabaseConnectionUtil.getDatabaseConnection();
		PreparedStatement preparedStatement = connection.prepareStatement("SELECT s.id as stateId, s.name as stateName, "
				+ "s.short_desc as stateShortName, s.country_id as countryId, c.country_name,c.short_desc as countryShortName "
				+ "FROM local_service_provider.state s inner join local_service_provider.country c on s.country_id = c.id "
				+ "order by c.country_name, s.name asc;");
		ResultSet resultSet = preparedStatement.executeQuery();
		State state = null;
		while (resultSet.next()) {
			Integer countryId = resultSet.getInt("countryId");
			Country country = countryMap.get(countryId);
			if(null == country) {
				country = new Country();
				country.setName(resultSet.getString("country_name"));
				country.setShortName(resultSet.getString("countryShortName"));
				countryMap.put(countryId, country);
			}
			List<State> states = stateMap.get(countryId);
			if(null == states || states.isEmpty()) {
				state = new State();
				state.setName(resultSet.getString("stateName"));
				state.setShortName(resultSet.getString("stateShortName"));
				states = new ArrayList<>();
				states.add(state);
			}else {
				state = new State();
				state.setName(resultSet.getString("stateName"));
				state.setShortName(resultSet.getString("stateShortName"));
				states.add(state);
			}
			stateMap.put(countryId, states);
		}
		
		Integer countrySize = countryMap.size(), currentSize = 1;
		
		for(Integer countryId : countryMap.keySet()) {
			Country country = countryMap.get(countryId);
			countryService.save(country);
			
			System.out.println("Country Size: "+countrySize+", Running Size : "+currentSize);
			
			List<State> states = stateMap.get(countryId);
			
			if(null != states && !states.isEmpty()) {
				
				Integer stateSize = states.size(), stateCurSize = 1;
						
				for (State stateObj : states) {
					stateObj.setCountryId(country.getId());
					stateService.save(stateObj);
					System.out.println("State Size: "+stateSize+", Running Size : "+stateCurSize);
					stateCurSize++;
				}
			}
			currentSize++;
		}
		
		
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("status", 1);		
		response.put("message","Success.!");
		TransactionContext context = responseGenerator.generateTransationContext(null);
		try {
			return responseGenerator.successResponse(context, response, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}*/
	
	
	@RequestMapping(value = "/try", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<?> loadMessage() throws Exception {
		System.out.println("Translated messages:");
		System.out.println(messageSource.getMessage("l1",
                null, Locale.GERMAN));
		System.out.println(messageSource.getMessage("l1",
                null, Locale.ENGLISH));

		System.out.println("Translated parameterized messages:");
		System.out.println(messageSource.getMessage("l2",
                new Object[] {"Paul Smith"}, Locale.GERMAN));
		System.out.println(messageSource.getMessage("l2",
                new Object[] {"Paul Smith"}, Locale.ENGLISH));
		return ResponseEntity.accepted().body("OK");
	} 
	
	
//	@ApiOperation(value = "send new password for user",response = Response.class)
//	@PutMapping(value = "/send/password", produces = "application/json")
//	public ResponseEntity<?> sendPassword (@ApiParam(value = "otpDto request payload") @RequestBody OtpDTO request,
//			@RequestHeader HttpHeaders httpHeader) throws Exception {
//		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
//		if (request.getUserNameOrEmail() == null || request.getUserNameOrEmail().isEmpty()) {
//			return responseGenerator.errorResponse(context, messagePropertySource.getMessage("payload.invalid"), HttpStatus.BAD_REQUEST);
//		}
//		
//		User userObj = userService.getUserByUserNameOrEmail(request.getUserNameOrEmail());
//		if(userObj == null) {
//			return responseGenerator.errorResponse(context, messagePropertySource.getMessage("username.email.invalid"), HttpStatus.BAD_REQUEST);
//		}
//		
//		String passowrd1 = RandomString.make(4);
//		
//		Random r = new Random();
//		Integer password2 = 100000 + r.nextInt(900000);
//		String password=passowrd1 + password2.toString();
//		
//		// send email
//		PasswordSendEvent mailEvent = new PasswordSendEvent(this,password);
//		mailEvent.setEmailTo(userObj.getEmail());
//		applicationEventPublisher.publishEvent(mailEvent);
//		
//		userObj.setPassword(PasswordUtil.getEncryptedPassword(password));
//		userObj.setForcePasswordChange(true);
//		userService.update(userObj);
//		
//		
//		try {
//			return responseGenerator.successResponse(context, messagePropertySource.getMessage("password.send"), HttpStatus.OK);
//		} catch (Exception e) {
//			e.printStackTrace();
//			logger.error(e.getMessage(), e);
//			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
//		}
//	}
	
	


	@ApiOperation(value = "Allows to passwordupdate existing agent setting.", response = Response.class)
	@RequestMapping(value = "/password/update", method = RequestMethod.PUT, produces = "application/json")
	public ResponseEntity<?> passwordUpdate(@ApiParam(value = "The password update request payload") @RequestBody OtpDTO otp,
			@RequestHeader HttpHeaders httpHeader ,Principal principle ) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		
		String  username = principle.getName();
		if(null == otp.getNewPassword() || otp.getNewPassword().isEmpty()) {
			return responseGenerator.errorResponse(context, messagePropertySource.getMessage("invalid.password"), HttpStatus.BAD_REQUEST);
		}
		
	    Optional<User> userObjOptional = userService.findByUserNameAndStatusAndIsDeletedFalseAndIsLockedFalse(username,Status.ACTIVE);
	    if(!userObjOptional.isPresent()) {
			return responseGenerator.errorResponse(context, messagePropertySource.getMessage("invalid.login"), HttpStatus.UNAUTHORIZED);
		}
	    User userObj = userObjOptional.get();
	    
	    userObj.setPassword(PasswordUtil.getEncryptedPassword(otp.getNewPassword()));
	    userObj.setForcePasswordChange(false);
	    userService.saveOrUpdate(userObj);

		try {
			return responseGenerator.successResponse(context, messagePropertySource.getMessage("force.password.update"), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		
	}
	
	@ApiOperation(value = "Allows to fetch user accessible page.", response = Response.class)
	@RequestMapping(value = "/get/user/access", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<?> getUserAccess(@RequestHeader HttpHeaders httpHeader ,Principal principle ) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		
		String userName = principle.getName();
		 User user = userService.findByUserNameAndIsDeletedFalseAndIsLockedFalse(userName).get();
		List<ModuleMaster> responseList = new ArrayList<ModuleMaster>();

		Optional<UserRole> userRole = userRoleService.findByUserId(user.getUserId());
		
		if(!userRole.isPresent()) {
			return responseGenerator.successGetResponse(context, messagePropertySource.getMessage("user.access"), responseList, HttpStatus.OK);
		}

		List<ModulePagePolicyMaster> modulePagePolicyList = modulePagePolicyMasterService.findAll();
		

		Map<UUID, List<ModulePagePolicyMaster>> pageIdmap = modulePagePolicyList.stream().collect(Collectors.groupingBy(p -> p.getModulePageMaster().getId()));

		List<ModulePageMaster> modulePageList = modulePageMasterService.findAll();

		Map<UUID, List<ModulePageMaster>> modulePageModuleIdMapp = modulePageList.stream().collect(Collectors.groupingBy(m -> m.getModuleObj().getId()));

		List<ModuleMaster> moduleList = moduleMasterService.findAll();
		Map<UUID, ModuleMaster> moduleIdMap = moduleList.stream().collect(Collectors.toMap(ModuleMaster::getId, m -> m));
		List<ModuleMaster> parentModuleList = moduleList.stream().filter(m -> m.getParentModuleId() != null).map(m -> m.getParentModuleId()).distinct().map(m -> moduleIdMap.get(m)).collect(Collectors.toList());
		ArrayList<UUID> idList = new ArrayList<UUID>();

		List<RolePagePolicyMapping> rolePagePolicyList = rolePagePolicyMappingService.findByRoleId(userRole.get().getRoleId());
		
		Map<UUID, UUID> rolePagePolicyIdMap = rolePagePolicyList.stream().collect(Collectors.toMap(r -> r.getModulePagePolicyObj().getId(), r -> r.getRoleMasterObj().getId()));
		

		for (ModuleMaster moduleMasterObj : parentModuleList) {
			if (idList.contains(moduleMasterObj.getId()))
				continue;
			idList.add(moduleMasterObj.getId());
			List<ModuleMaster> childList = moduleList.stream().filter(m -> m.getParentModuleId() != null).filter(c -> c.getParentModuleId().equals(moduleMasterObj.getId())).collect(Collectors.toList());

			List<ModuleMaster> childList1=new ArrayList<>();
			for (ModuleMaster child : childList) {
				idList.add(child.getId());
				List<ModulePageMaster> pageList = modulePageModuleIdMapp.get(child.getId());
				List<ModulePageMaster> pageList1 =new ArrayList<>();
				for (ModulePageMaster page : null != pageList ? pageList : new ArrayList<ModulePageMaster>()) {
					if(pageIdmap.get(page.getId())!=null){
						if(pageIdmap.get(page.getId()).stream().anyMatch(mppm->rolePagePolicyIdMap.get(mppm.getId()) != null)){
							page.setIsChecked(true);
							pageList1.add(page);
						}
						
					}
					
				}
				if(pageList1!=null && !pageList1.isEmpty()){
					child.setPageList(pageList1);
					child.setIsChecked(true);
					childList1.add(child);
				}

			}
			
			if(!childList1.isEmpty() && childList1 !=null){
				moduleMasterObj.setSubModules(childList1);
				moduleMasterObj.setIsChecked(true);
				responseList.add(moduleMasterObj);
			}

		}

		for (ModuleMaster module : moduleList) {
			if (idList.contains(module.getId()))
				continue;
			idList.add(module.getId());

			List<ModulePageMaster> pageList = modulePageModuleIdMapp.get(module.getId());
			List<ModulePageMaster> pageList1 =new ArrayList<>();
			for (ModulePageMaster page : null != pageList ? pageList : new ArrayList<ModulePageMaster>()) {
				if(pageIdmap.get(page.getId())!=null){
					if(pageIdmap.get(page.getId()).stream().anyMatch(mppm->rolePagePolicyIdMap.get(mppm.getId()) != null)){
						page.setIsChecked(true);
						pageList1.add(page);
					}
					
				}

			}
			
			if(!pageList1.isEmpty() && pageList1!=null){
				module.setPageList(pageList1);
				module.setIsChecked(true);
				responseList.add(module);
			}

		}
		try {
			return responseGenerator.successGetResponse(context, messagePropertySource.getMessage("user.access"), responseList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
			
	}
	
	
	@ApiOperation(value = "send new password for user", response = Response.class)
	@PutMapping(value = "/send/password", produces = "application/json")
	public ResponseEntity<?> sendPassword(@ApiParam(value = "otpDto request payload") @RequestBody SendOTPDTO request,
			@RequestHeader HttpHeaders httpHeader) throws Exception {
		TransactionContext context = responseGenerator.generateTransationContext(httpHeader);
		if (request.getUserNameOrEmailOrPhoneNumber() == null || request.getUserNameOrEmailOrPhoneNumber().isEmpty()) {
			return responseGenerator.errorResponse(context, messagePropertySource.getMessage("payload.invalid"),
					HttpStatus.BAD_REQUEST);
		}

		 User userObj = userService.findByUserNameOrEmailOrPhoneNoAndIsDeletedFalseAndIsLockedFalse(request.getUserNameOrEmailOrPhoneNumber(),request.getUserNameOrEmailOrPhoneNumber(), request.getUserNameOrEmailOrPhoneNumber()).get();
		if (userObj == null) {
			return responseGenerator.errorResponse(context, messagePropertySource.getMessage("username.email.invalid"),
					HttpStatus.BAD_REQUEST);
		}

		String passowrd1 = RandomString.make(4);

		Random r = new Random();
		Integer password2 = 100000 + r.nextInt(900000);
		String password = passowrd1 + password2.toString();

		// send email
		PasswordSendEvent mailEvent = new PasswordSendEvent(this, password);
		mailEvent.setEmailTo(userObj.getEmail());
		applicationEventPublisher.publishEvent(mailEvent);

		userObj.setPassword(PasswordUtil.getEncryptedPassword(password));
		userObj.setForcePasswordChange(true);
		userService.saveOrUpdate(userObj);

		try {
			return responseGenerator.successResponse(context, messagePropertySource.getMessage("password.send"), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Allows to change current password", response = Response.class)
	@PutMapping(value = "/change/password", produces = "application/json")
	public ResponseEntity<?> update(
			@ApiParam(value = "Payload for change current password ") @RequestBody UserUpdateDTO request,
			@RequestHeader HttpHeaders httpHeaders) throws Exception {

		TransactionContext context = responseGenerator.generateTransationContext(httpHeaders);
		Optional<User> userOptional = userService.findByPasswordAndIsDeletedFalseAndIsLockedFalse(PasswordUtil.getEncryptedPassword(request.getOldPassword()));

		if (!userOptional.isPresent()) {
			return responseGenerator.errorResponse(context, messagePropertySource.getMessage("invalid.user.password"),HttpStatus.BAD_REQUEST);
		}
		User userObj = userOptional.get();
		if(!request.getNewPassword().equals(request.getConfirmPassword())) {
			return responseGenerator.errorResponse(context, messagePropertySource.getMessage("invalid.new.and.confirm.password"),HttpStatus.BAD_REQUEST);
		}
		userObj.setPassword(PasswordUtil.getEncryptedPassword(request.getNewPassword()));
		userService.saveOrUpdate(userObj);

		try {
			return responseGenerator.successResponse(context, messagePropertySource.getMessage("password.update"),HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return responseGenerator.errorResponse(context, e.getMessage(), HttpStatus.BAD_REQUEST);
		}

	}

}
