/*******************************************************************************
 * Copyright (c) 2009, 2016 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *  
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package it.greenvulcano.gvesb.gviamx.api;

import java.util.Optional;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cxf.jaxrs.utils.JAXRSUtils;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.apache.cxf.security.SecurityContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.gvesb.gviamx.domain.EmailChangeRequest;
import it.greenvulcano.gvesb.gviamx.domain.PasswordResetRequest;
import it.greenvulcano.gvesb.gviamx.domain.SignUpRequest;
import it.greenvulcano.gvesb.gviamx.service.internal.EmailChangeManager;
import it.greenvulcano.gvesb.gviamx.service.internal.PasswordResetManager;
import it.greenvulcano.gvesb.gviamx.service.internal.SignUpManager;
import it.greenvulcano.gvesb.iam.domain.User;
import it.greenvulcano.gvesb.iam.domain.UserInfo;
import it.greenvulcano.gvesb.iam.exception.InvalidPasswordException;
import it.greenvulcano.gvesb.iam.exception.InvalidRoleException;
import it.greenvulcano.gvesb.iam.exception.InvalidUsernameException;
import it.greenvulcano.gvesb.iam.exception.UserExistException;
import it.greenvulcano.gvesb.iam.exception.UserNotFoundException;
import it.greenvulcano.gvesb.iam.service.UsersManager.Authority;
import it.greenvulcano.util.crypto.CryptoHelper;
import it.greenvulcano.util.crypto.CryptoHelperException;
import it.greenvulcano.util.crypto.CryptoUtilsException;

@CrossOriginResourceSharing
public class GVAccountControllerRest {
	private final static Logger LOG = LoggerFactory.getLogger(GVAccountControllerRest.class);
	
	private SignUpManager signupManager;
	private PasswordResetManager passwordResetManager;
	private EmailChangeManager emailChangeManager;
		
	public void setPasswordResetManager(PasswordResetManager passwordResetManager) {
		this.passwordResetManager = passwordResetManager;
	}	
	
	public void setSignupManager(SignUpManager signupManager) {
		this.signupManager = signupManager;
	}
	
	public void setEmailChangeManager(EmailChangeManager emailChangeManager) {
		this.emailChangeManager = emailChangeManager;
	}
	
	@Path("/signup")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({Authority.ADMINISTRATOR, Authority.MANAGER, Authority.CLIENT})
	public Response checkResgistrationStatus(@QueryParam("check") String email) {
			
		JSONObject status = new JSONObject();
		if (email != null &&  email.matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")) {
			status.put("email", email);
		} else {
			return Response.status(Status.BAD_REQUEST).entity("Missing or invalid value for parameter 'check' ").build();
			
		}
		
		try {
			signupManager.getUsersManager().getUser(email);
			status.put("status", "CONFIRMED");
		} catch (UserNotFoundException userNotFoundException) {
			
			try {
				signupManager.retrieveSignUpRequest(email, "");
				throw new RuntimeException("Invalid request token");
			} catch (SecurityException e) {
				status.put("status", "PENDING");					
			} catch (IllegalArgumentException signupRequestNotExistException) {
				status.put("status", "UNKNOWN");
			}
		} catch (Exception e) {
			LOG.error("Fatal error checking signup status", e);
			
			return Response.serverError().build();
		}
		
		return Response.ok(status.toString()).build();
		
	}
	
	@Path("/signup")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@RolesAllowed({Authority.ADMINISTRATOR, Authority.MANAGER, Authority.CLIENT})
	public Response submitSignUpRequest(String request) {
		Response response = null;
		
		try {
			JSONObject jsonData = new JSONObject(request);
			String password = jsonData.optString("password", "") ;
						
		    if (!password.matches(User.PASSWORD_PATTERN)) {
		    	throw new InvalidPasswordException(password);
		    }
			
			jsonData.put("password", CryptoHelper.encrypt(CryptoHelper.DEFAULT_KEY_ID, password, true ));
			signupManager.createSignUpRequest(jsonData.optString("email"), password, jsonData.toString().getBytes());
		
			response = Response.ok().build();
		} catch (InvalidPasswordException e) {
			LOG.warn("Error processing signup request", e);
			response = Response.status(Status.FORBIDDEN).entity(e.getMessage()).build();			
		} catch (JSONException jsonException) {
			LOG.warn("Error processing signup request", jsonException);
			response = Response.status(Status.BAD_REQUEST).entity("Invalid json").build();
		} catch (IllegalArgumentException e) {
			LOG.warn("Error processing signup request", e);
			response = Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();		
		} catch (UserExistException e) {
			LOG.warn("Error processing signup request", e);
			response = Response.status(Status.CONFLICT).entity("Email address in use ").build();
		} catch (CryptoHelperException|CryptoUtilsException e){		
			LOG.error("GVCryptoHelper configuration missing or invalid", e);			
			response = Response.status(Status.SERVICE_UNAVAILABLE).build();
			
		} catch (Exception e) {
			LOG.error("Fatal error creating signup request", e);			
			response = Response.serverError().build();
		}
		
		return response;
		
	}
	
	@Path("/signup")
	@PUT
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@RolesAllowed({Authority.ADMINISTRATOR, Authority.MANAGER, Authority.CLIENT})
	public Response confirmSignUpRequest(@FormParam("email")String email, @FormParam("token")String token){
				
		Response response = null;
		
		try {
			
			Optional.ofNullable(email).orElseThrow(()->new IllegalArgumentException("Required parameter: email"));
			Optional.ofNullable(token).orElseThrow(()->new IllegalArgumentException("Required parameter: token"));
						
			SignUpRequest signupRequest = signupManager.retrieveSignUpRequest(email, token);			
			
			String password = CryptoHelper.decrypt(CryptoHelper.DEFAULT_KEY_ID, (String) signupRequest.getActionData().get("password"), false) ;
			
			User user = signupManager.getUsersManager().createUser(email, password);
		
			user.setUserInfo(new UserInfo());
			user.getUserInfo().setEmail(email);
			
			try {
				JSONObject object = new JSONObject(new String(signupRequest.getRequest(), "UTF-8"));
				String first = object.getJSONObject("client").getString("first_name");
				String last = object.getJSONObject("client").getString("last_name");
				
				String fullname = first.concat(" ").concat(last);
				user.getUserInfo().setFullname(fullname);
			} catch (Exception e) {
			    LOG.debug("Fail to retrieve fullname", e);
			}
							
			signupManager.getUsersManager().updateUser(email, user.getUserInfo(), user.getRoles(), true, false);
						
			signupManager.consumeSignUpRequest(signupRequest);
			response = Response.ok().build();
		} catch (IllegalArgumentException|SecurityException e) {
			LOG.warn("Error performing signup", e);
			response = Response.status(Status.NOT_FOUND).build();	
		} catch (InvalidUsernameException e) {
			LOG.warn("Error performing signup", e);
			response = Response.status(Status.BAD_REQUEST).entity("Invalid email").build();
		} catch (InvalidPasswordException e) {
			LOG.warn("Error performing signup", e);
			response = Response.status(Status.BAD_REQUEST).entity("Invalid password").build();		
		} catch (Exception e) {
			LOG.error("Fatal error performing signup", e);
			signupManager.getUsersManager().deleteUser(email);
			response = Response.serverError().build();
		}
		
		return response;
	}
	
	@Path("/restore")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@RolesAllowed({Authority.ADMINISTRATOR, Authority.MANAGER, Authority.CLIENT})
	public Response submitResetPasswordRequest(@FormParam("email") String email){
		Response response = null;
		try {
			
			passwordResetManager.createPasswordResetRequest(Optional.ofNullable(email).orElseThrow(()->new IllegalArgumentException("Required parameter: email")));
			response = Response.ok().build();
			
		} catch (UserNotFoundException e) {
			LOG.warn("Error processing password reset request", e);
			response = Response.status(Status.NOT_FOUND).entity("No matching account found").build();
		} catch (IllegalArgumentException e) {
			LOG.warn("Error processing password reset request", e);
			response = Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();		
		}
		
		return response;
				
	}
	
	@Path("/restore")
	@PUT
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@RolesAllowed({Authority.ADMINISTRATOR, Authority.MANAGER, Authority.CLIENT})
	public Response consumeResetPasswordRequest(@FormParam("email")String email, @FormParam("token")String token, @FormParam("password")String password) {
		
		Response response = null;
		try {
			Optional.ofNullable(email).orElseThrow(()->new IllegalArgumentException("Required parameter: email"));
			Optional.ofNullable(token).orElseThrow(()->new IllegalArgumentException("Required parameter: token"));
			Optional.ofNullable(password).orElseThrow(()->new IllegalArgumentException("Required parameter: password"));
			
			PasswordResetRequest passwordResetRequest =  passwordResetManager.retrievePasswordResetRequest(email,token);
			
			passwordResetManager.getUsersManager().resetUserPassword(email, password);
			passwordResetManager.getUsersManager().setUserExpiration(email, false);
			
			passwordResetManager.consumePasswordResetRequest(passwordResetRequest);
			response = Response.ok().build();
			
		} catch (UserNotFoundException|SecurityException e) {
			LOG.warn("Error performing password reset", e);
			response = Response.status(Status.NOT_FOUND).entity("No matching account found").build();
		} catch (InvalidPasswordException e) {
			LOG.warn("Error performing password reset", e);
			response = Response.status(Status.FORBIDDEN).entity("Invalid password").build();
		} catch (IllegalArgumentException e) {
			LOG.warn("Error performing password reset", e);
			response = Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();		
		}
		
		return response;
		
	}
	
	@Path("/update")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@PermitAll
	public Response submitChangeEmailRequest(@FormParam("email")String email, @FormParam("new_email")String newEmail){
		Response response = null;
		
		try {
			Optional.ofNullable(email).orElseThrow(()->new IllegalArgumentException("Required parameter: email"));
			Optional.ofNullable(newEmail).orElseThrow(()->new IllegalArgumentException("Required parameter: new_email"));
				
			SecurityContext securityContext = JAXRSUtils.getCurrentMessage().get(SecurityContext.class);			
				
			if (isGrantedAuthority(securityContext, email)){
				emailChangeManager.createEmailChangeRequest(email, newEmail);
				response = Response.ok().build();
			} else {
				response = Response.status(Status.NOT_FOUND).entity("No matching account found").build();
			}					
			
		} catch (UserNotFoundException e) {
			LOG.warn("Error processing username update request", e);
			response = Response.status(Status.NOT_FOUND).entity("No matching account found").build();
		} catch (IllegalArgumentException e) {
			LOG.warn("Error processing username update request", e);
			response = Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();		
		} 
		
		return response;
		
	}
	
	@Path("/update")
	@PUT
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@PermitAll
	public Response consumeChangeEmailRequest(@FormParam("email")String email, @FormParam("token")String token) {
		Response response = null;
		
		try {
			
			Optional.ofNullable(email).orElseThrow(() -> new IllegalArgumentException("Required parameter: email"));
			Optional.ofNullable(token).orElseThrow(()->new IllegalArgumentException("Required parameter: token"));
			
			SecurityContext securityContext = JAXRSUtils.getCurrentMessage().get(SecurityContext.class);
			if (isGrantedAuthority(securityContext, email)){
				EmailChangeRequest request = emailChangeManager.retrieveEmailChangeRequest(email, token);		
				emailChangeManager.getUsersManager().updateUsername(request.getUser().getUsername(), email);
								
				try {
					UserInfo userInfo = request.getUser().getUserInfo();
					userInfo.setEmail(email);
					emailChangeManager.getUsersManager().updateUser(email, userInfo, request.getUser().getRoles(), request.getUser().isEnabled(), request.getUser().isExpired());
				} catch (Exception e) {
					LOG.warn("Fail to update userInfo", e);
				}
				
				emailChangeManager.consumeEmailChangeRequest(request);
				response = Response.ok().build();
			} else {
				throw new SecurityException();
			}
		} catch (UserNotFoundException|SecurityException e) {
			LOG.warn("Error performing username update", e);
			response = Response.status(Status.NOT_FOUND).entity("No matching account found").build();	
		} catch (InvalidUsernameException|IllegalArgumentException e) {
			LOG.warn("Error performing username update", e);
			response = Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();		
		} 		
		
		return response;
		
	}
	
	private boolean isGrantedAuthority(SecurityContext securityContext, String email){
	   return  securityContext!=null && securityContext.getUserPrincipal() !=null &&
		       (securityContext.isUserInRole(Authority.ADMINISTRATOR) ||
		        securityContext.isUserInRole(Authority.MANAGER) ||
		        securityContext.isUserInRole(Authority.CLIENT) ||
				securityContext.getUserPrincipal().getName().equals(email));
	}
	
	@Path("/grant")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@RolesAllowed({Authority.ADMINISTRATOR, Authority.MANAGER})
	public void grantRole(@FormParam("username")String username, @FormParam("role")String role) {
		
		try {
			SecurityContext securityContext = JAXRSUtils.getCurrentMessage().get(SecurityContext.class);
			if (!securityContext.isUserInRole(Authority.ADMINISTRATOR) && Authority.entries.contains(role) ) {
				throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).entity(String.format("Invalid role %s", role)).build());
			}
			
			signupManager.getUsersManager().addRole(username, role);
		} catch (InvalidRoleException e) {
			LOG.warn("Error performing grant role", e);
			throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(String.format("Invalid role %s", role)).build());
			
		} catch (UserNotFoundException e) {
			LOG.warn("Error performing grant role", e);
			throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(String.format("User %s not found",username)).build());
		}
	}
	
	@Path("/grant")
	@DELETE
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@RolesAllowed({Authority.ADMINISTRATOR, Authority.MANAGER})
	public void revokeRole(@FormParam("username")String username, @FormParam("role")String role) {
		
		try {
			SecurityContext securityContext = JAXRSUtils.getCurrentMessage().get(SecurityContext.class);
			if (!securityContext.isUserInRole(Authority.ADMINISTRATOR) && Authority.entries.contains(role) ) {
				throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).entity(String.format("Invalid role %s", role)).build());
			}
			
			signupManager.getUsersManager().revokeRole(username, role);
			
		} catch (UserNotFoundException e) {
			LOG.warn("Error performing revoke role", e);
			throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(String.format("User %s not found",username)).build());
		}
	}

}
