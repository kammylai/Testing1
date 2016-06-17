package com.force.example.fulfillment.order.controller;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.force.example.fulfillment.order.model.Order;
import com.force.example.fulfillment.order.service.OrderService;

@Controller
@RequestMapping(value="/order")
public class OrderController {
	
	@Autowired
	private OrderService orderService;
	
	private Validator validator;
	
	@Autowired
	public OrderController(Validator validator) {
		this.validator = validator;
	}
	
	@RequestMapping(method=RequestMethod.POST)
	public @ResponseBody List<? extends Object> create(@Valid @RequestBody Order[] orders, HttpServletResponse response) {
		boolean failed = false;
		List<Map<String, String>> failureList = new LinkedList<Map<String, String>>();
		for (Order order: orders) {
			Set<ConstraintViolation<Order>> failures = validator.validate(order);
			if (failures.isEmpty()) {
				Map<String, String> failureMessageMap = new HashMap<String, String>();
				if (! orderService.findOrderById(order.getId()).isEmpty()) {					
					failureMessageMap.put("id", "id already exists in database");					
					failed = true;
				}
				failureList.add(failureMessageMap);
			} else {
				failureList.add(validationMessages(failures));
				failed = true;
			}
		}
		if (failed) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return failureList;
		} else {
			List<Map<String, Object>> responseList = new LinkedList<Map<String, Object>>();
			for (Order order: orders) {
				orderService.addOrder(order);
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("id", order.getId());
				map.put("order_number", order.getOrderId());
				responseList.add(map);
			}
			return responseList;
		}
	}
	
	@RequestMapping(method=RequestMethod.GET)
	public @ResponseBody List<Order> getOrders() {
		return orderService.listOrders();
	}

	@RequestMapping(value="{orderId}", method=RequestMethod.GET)
	public @ResponseBody Order getOrder(@PathVariable Integer orderId) {
		Order order = orderService.findOrder(orderId);
		if (order == null) {
			throw new ResourceNotFoundException(orderId);
		}
		return order;
	}
	
	@RequestMapping(value="{orderId}", method=RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.OK)
	public void deleteOrder(@PathVariable Integer orderId) {
		orderService.removeOrder(orderId);
	}
	
	//Kammy start:
	@RequestMapping(method=RequestMethod.POST)
	public @ResponseBody List<? extends Object> create(@Valid @RequestBody Order[] orders, HttpServletResponse response) {
		initParams = { 
		@WebInitParam(name = "clientId", value = 
				"3MVG9ZL0ppGP5UrCwpSxUGhAMnfBs36FlDfQp8OWfxNJI_bmnj5q3K1DYCM3WAUuFnTuwzNxiY3vMNpFlCFBZ"),
		@WebInitParam(name = "clientSecret", value = "3474700791504880027"),
		@WebInitParam(name = "redirectUri", value = 
				"https://aqueous-shore-92031.herokuapp.com/_auth"),
		@WebInitParam(name = "environment", value = 
				"https://elufa-sdfc-poc-dev-ed.my.salesforce.com/services/oauth2/token")  }
		 
		HttpClient httpclient = new HttpClient();
		PostMethod post = new PostMethod(environment);
		post.addParameter("code",code);
		post.addParameter("grant_type","authorization_code");

		   /** For session ID instead of OAuth 2.0, use "grant_type", "password" **/
		post.addParameter("client_id",3MVG9ZL0ppGP5UrCwpSxUGhAMnfBs36FlDfQp8OWfxNJI_bmnj5q3K1DYCM3WAUuFnTuwzNxiY3vMNpFlCFBZ);
		post.addParameter("client_secret",3474700791504880027);
		post.addParameter("redirect_uri",https://aqueous-shore-92031.herokuapp.com/_auth);
		
		
	   //exception handling removed for brevity...
	  //this is the post from step 2     
	  httpclient.executeMethod(post);
		 String responseBody = post.getResponseBodyAsString();
	   
	  String accessToken = null;
	  JSONObject json = null;
	   try {
		   json = new JSONObject(responseBody);
			 accessToken = json.getString("access_token");
			 issuedAt = json.getString("issued_at");
			 /** Use this to validate session 
			  * instead of expiring on browser close.
			  */
									
		 } catch (JSONException e) {
				e.printStackTrace();
		 }
 
		 HttpServletResponse httpResponse = (HttpServletResponse)response;
		  Cookie session = new Cookie(ACCESS_TOKEN, accessToken);
		 session.setMaxAge(-1); //cookie not persistent, destroyed on browser exit
		 httpResponse.addCookie(session);
		
		
		
		  PostMethod m = new PostMethod("https://elufa-sdfc-poc-dev-ed.my.salesforce.com/a0D" +  "?_HttpMethod=PATCH");
			
		  m.setRequestHeader("Authorization", "OAuth " + accessToken);

		  Map<String, Object> accUpdate = new HashMap<String, Object>();
		  accUpdate.put("Status__c", "Patch test");
		  ObjectMapper mapper = new ObjectMapper();
		  m.setRequestEntity(new StringRequestEntity(mapper.writeValueAsString(accUpdate), "application/json", "UTF-8"));

		  HttpClient c = new HttpClient();
		  int sc = c.executeMethod(m);
		  System.out.println("PATCH call returned a status code of " + sc);
		  if (sc > 299) {
			// deserialize the returned error message
			List<ApiError> errors = mapper.readValue(m.getResponseBodyAsStream(), new TypeReference<List<ApiError>>() {} );
			for (ApiError e : errors)
			  System.out.println(e.errorCode + " " + e.message);
		  }
	}

	
	
	//Kammy end.
	
	// internal helper
	private Map<String, String> validationMessages(Set<ConstraintViolation<Order>> failureSet) {
		Map<String, String> failureMessageMap = new HashMap<String, String>();
		for (ConstraintViolation<Order> failure : failureSet) {
			failureMessageMap.put(failure.getPropertyPath().toString(), failure.getMessage());
		}
		return failureMessageMap;
	}
}
