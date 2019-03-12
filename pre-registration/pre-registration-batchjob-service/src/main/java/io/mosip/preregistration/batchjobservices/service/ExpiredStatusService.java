/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.batchjobservices.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.util.DateUtils;
import io.mosip.preregistration.batchjobservices.entity.DemographicEntity;
import io.mosip.preregistration.batchjobservices.entity.RegistrationBookingEntity;
import io.mosip.preregistration.batchjobservices.exceptions.util.BatchServiceExceptionCatcher;
import io.mosip.preregistration.batchjobservices.repository.dao.BatchServiceDAO;
import io.mosip.preregistration.core.code.StatusCodes;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;

/**
 * @author Kishan Rathore
 * @since 1.0.0
 *
 */
@Component
public class ExpiredStatusService {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ConsumedStatusService.class);

	/** The Constant LOGDISPLAY. */
	private static final String LOGDISPLAY = "{} - {}";
	
	@Value("${mosip.utc-datetime-pattern}")
	private String utcDateTimePattern;

	@Autowired
	private BatchServiceDAO batchServiceDAO;

	/**
	 * @return Response dto
	 */
	public MainResponseDTO<String> expireAppointments() {

		LocalDate currentDate = LocalDate.now();
		MainResponseDTO<String> response = new MainResponseDTO<>();
		List<RegistrationBookingEntity> bookedPreIdList = new ArrayList<>();
		try {
			bookedPreIdList = batchServiceDAO.getAllOldDateBooking(currentDate);

			bookedPreIdList.forEach(iterate -> {
				String preRegId = iterate.getBookingPK().getPreregistrationId();
				DemographicEntity demographicEntity = batchServiceDAO.getApplicantDemographicDetails(preRegId);
				if (demographicEntity != null) {

					if (demographicEntity.getStatusCode().equals(StatusCodes.BOOKED.getCode())) {
						demographicEntity.setStatusCode(StatusCodes.EXPIRED.getCode());
						batchServiceDAO.updateApplicantDemographic(demographicEntity);
					}
					LOGGER.info(LOGDISPLAY,
							"Update the status successfully into Registration Appointment table and Demographic table");

				}
			});

		} catch (Exception e) {
			new BatchServiceExceptionCatcher().handle(e);
		}
		response.setResTime(getCurrentResponseTime());
		response.setStatus(true);
		response.setResponse("Registration appointment status updated to expired successfully");
		return response;
	}

	public String getCurrentResponseTime() {
		return DateUtils.formatDate(new Date(System.currentTimeMillis()), utcDateTimePattern);
	}

}
