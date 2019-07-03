update cmn_application_preference set preference_name = 'dds_ws_endpoint' where preference_name = 'dds_endpoint';
update cmn_application_preference set preference_value = 'full' where preference_name = 'dds_mode';
update tm_object_class_validator SET value = 'net.ihe.gazelle.dicom.evs.api.Dicom3tools' where value = 'net.ihe.gazelle.dicom.Dicom3tools' ;
update tm_object_class_validator SET value = 'net.ihe.gazelle.dicom.evs.api.Pixelmed' where value = 'net.ihe.gazelle.dicom.Pixelmed' ;
update tm_object_class_validator SET value = 'net.ihe.gazelle.dicom.evs.api.Dicom4che' where value = 'net.ihe.gazelle.dicom.Dicom4che' ;
update tm_object_method_validator SET value = 'validate'    where name = 'dicom3tools ::: validation of dicom file' ;