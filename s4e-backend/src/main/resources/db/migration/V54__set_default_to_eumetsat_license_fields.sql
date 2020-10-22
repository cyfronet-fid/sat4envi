UPDATE app_user SET eumetsat_license = false WHERE eumetsat_license IS NULL;
UPDATE institution SET eumetsat_license = false WHERE eumetsat_license IS NULL;
