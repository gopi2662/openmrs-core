package org.openmrs.web.controller.patient;

import java.text.DateFormat;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientAddress;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientName;
import org.openmrs.api.PatientService;
import org.openmrs.api.UserService;
import org.openmrs.context.Context;
import org.openmrs.web.Constants;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

import sun.security.krb5.internal.p;

public class PatientFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
    
	/**
	 * 
	 * Allows for other Objects to be used as values in input tags.
	 *   Normally, only strings and lists are expected 
	 * 
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest, org.springframework.web.bind.ServletRequestDataBinder)
	 */
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
        //NumberFormat nf = NumberFormat.getInstance(new Locale("en_US"));
        //binder.registerCustomEditor(java.lang.Integer.class,
        //        new CustomNumberEditor(java.lang.Integer.class, nf, true));
		binder.registerCustomEditor(java.lang.Integer.class, 
				new CustomNumberEditor(java.lang.Integer.class, true));
        binder.registerCustomEditor(java.util.Date.class, 
        		new CustomDateEditor(DateFormat.getDateInstance(DateFormat.SHORT), true));
	}

	/**
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#processFormSubmission(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object object, BindException errors) throws Exception {
	
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		Patient patient = (Patient)object;
		
		if (context != null && context.isAuthenticated()) {
			
			PatientService ps = context.getPatientService();
			Object[] objs = null;
			// Patient Identifiers 

				/* TODO uncomment after patient_identifier.patient_identifier_id added 
				//Spring doesn't handle objects that are simply removed from the 
				//	request so it is done manually here
				Object[] objs = patient.getIdentifiers().toArray();
				for (int i = 0; i < objs.length; i++ ) {
					if (request.getParameter("identifiers[" + i + "].patientIdentifierId") == null)
						patient.removeIdentifier((PatientIdentifier)objs[i]);
				}
				*/
				String[] ids = request.getParameterValues("identifier");
				String[] idTypes = request.getParameterValues("identifierType");
				String[] locs = request.getParameterValues("location");
				
				for (int i = 0; i < ids.length; i++) {
					if (ids[i] != "") { //skips invalid and blank address data box
						PatientIdentifier pi = new PatientIdentifier();
						pi.setIdentifier(ids[i]);
						pi.setIdentifierType(ps.getPatientIdentifierType(Integer.valueOf(idTypes[i])));
						pi.setLocation(ps.getLocation(Integer.valueOf(locs[i])));
						patient.addIdentifier(pi);
					}
				}
				
			// Patient Address
			
				String [] add1s = request.getParameterValues("address1");
				String [] add2s = request.getParameterValues("address2");
				String [] cities = request.getParameterValues("cityVillage");
				String [] states = request.getParameterValues("stateProvince");
				String [] countries = request.getParameterValues("country");
				String [] lats = request.getParameterValues("latitude");
				String [] longs = request.getParameterValues("longitude");
				
				for (int i = 0; i < add1s.length; i++) {
					if (add1s[i] != "") { //skips invalid and blank address data box
						PatientAddress pa = new PatientAddress();
						pa.setAddress1(add1s[i]);
						pa.setAddress2(add2s[i]);
						pa.setCityVillage(cities[i]);
						pa.setStateProvince(states[i]);
						pa.setCountry(countries[i]);
						pa.setLatitude(lats[i]);
						pa.setLongitude(longs[i]);
						patient.addAddress(pa);
					}
				}
				
			// Patient Names

				objs = patient.getNames().toArray();
				for (int i = 0; i < objs.length; i++ ) {
					if (request.getParameter("names[" + i + "].patientNameId") == null)
						patient.removeName((PatientName)objs[i]);
				}

				//String[] prefs = request.getParameterValues("preferred");  unreliable form info
				String[] gNames = request.getParameterValues("givenName");
				String[] mNames = request.getParameterValues("middleName");
				String[] fNamePrefixes = request.getParameterValues("familyNamePrefix");
				String[] fNames = request.getParameterValues("familyName");
				String[] fName2s = request.getParameterValues("familyName2");
				String[] fNameSuffixes = request.getParameterValues("familyNameSuffix");
				String[] degrees = request.getParameterValues("degree");
				
				
				for (int i = 0; i < gNames.length; i++) {
					if (gNames[i] != "") { //skips invalid and blank address data box
						PatientName pn = new PatientName();
						pn.setPreferred(false);
						pn.setGivenName(gNames[i]);
						pn.setMiddleName(mNames[i]);
						pn.setFamilyNamePrefix(fNamePrefixes[i]);
						pn.setFamilyName(fNames[i]);
						pn.setFamilyName2(fName2s[i]);
						pn.setFamilyNameSuffix(fNameSuffixes[i]);
						pn.setDegree(degrees[i]);
						patient.addName(pn);
					}
				}
			// Patient Info 
				patient.setTribe(ps.getTribe(Integer.valueOf(request.getParameter("tribe"))));
		}
		
		return onSubmit(request, response, patient, errors);
	}
	

	/**
	 * 
	 * The onSubmit function receives the form/command object that was modified
	 *   by the input form and saves it to the db
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj, BindException errors) throws Exception {
		
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		Patient patient = (Patient)obj;
				
		if (context != null && context.isAuthenticated()) {
			
			
			boolean isNew = (patient.getPatientId() == null);
			
			context.getPatientService().updatePatient(patient);
			
			String view = getSuccessView();
			if (isNew)
				view = request.getContextPath() + "/formentry/index.htm?patientId=" + patient.getPatientId(); 
			
			httpSession.setAttribute(Constants.OPENMRS_MSG_ATTR, "Patient info saved.");
			return new ModelAndView(new RedirectView(view));
		}
		
		return new ModelAndView(new RedirectView(getFormView()));
	}

	/**
	 * 
	 * This is called prior to displaying a form for the first time.  It tells Spring
	 *   the form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
    protected Object formBackingObject(HttpServletRequest request) throws ServletException {

		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		Patient patient = new Patient();
		
		if (context != null && context.isAuthenticated()) {
			PatientService ps = context.getPatientService();
			String patientId = request.getParameter("patientId");
	    	if (patientId != null)
	    		patient = ps.getPatient(Integer.valueOf(patientId));
		}
        return patient;
    }

	/**
	 * 
	 * Called prior to form display.  Allows for data to be put 
	 * 	in the request to be used in the view
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest)
	 */
	protected Map referenceData(HttpServletRequest arg0) throws Exception {
		
		Map<String, Object> map = new HashMap<String, Object>();

		// empty objects used to create blank template in the view
		map.put("emptyIdentifier", new PatientIdentifier());
		map.put("emptyName", new PatientName());
		map.put("emptyAddress", new PatientAddress());
		
		return map;
	}    
	
}