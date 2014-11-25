<?php

  // Push Notification Info
  define("GCM_SenderID", "385041079398");
  define("GCM_APIKey", "AIzaSyCjXiSeTMvzc9uqDa9hP5sYsslSZXQpKlY");

  sendPushNotification(array("123", "321"));

  function sendPushNotification($gcmRegistrationIDs)
  {
    $url = "https://android.googleapis.com/gcm/send";

    $data = array(
      'registration_ids'  => $gcmRegistrationIDs,
      'collapse_key'      => "note"
    );

    $options = array(
      'http' => array(
        'header'  => "Content-Type:application/json\r\nAuthorization:key=" . GCM_APIKey,
        'method'  => 'POST',
        'content' => http_build_query($data),
      ),
    );

    $context  = stream_context_create($options);
    $result = file_get_contents($url, false, $context);

    var_dump($result);
  }

  // Database Configuration
  define("Database_NAME", "davisengeler_daycare");
  define("Database_HOST", "localhost");
  define("Database_USER", "daycare");
  define("Database_PASS", "engelerhetzel");

  function connectDB()
  {
    $connection = mysqli_connect(Database_HOST, Database_USER, Database_PASS, Database_NAME) or die("Could not connect to database");
    return $connection;
  }

  // Provides a check to make sure the device making calls to the API is a verified device
  function deviceIsVerified($database, $deviceID)
  {
    if ($result = mysqli_query($database, "CALL device_verified($deviceID)"))
    {
      $row = mysqli_fetch_array($result);
      if ($row["Verified"] == 1)
      {
        // the device is verified
        return true;
      }
      else
      {
        // the device was denied or still pending
        return false;
      }
    }
    else
    {
      // Had an issue with the database
      return false;
    }
  }

?>
