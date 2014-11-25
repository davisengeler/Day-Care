<?php

  // Push Notification Info
  define("GCM_SenderID", "385041079398");
  define("GCM_APIKey", "AIzaSyCjXiSeTMvzc9uqDa9hP5sYsslSZXQpKlY");

  sendPushNotification(array("APA91bF2_TLLr01h8ppMkkwdpZN4mxJ--IynAGaPREeOp"));

  function sendPushNotification($gcmRegistrationIDs)
  {
    $apiKey = GCM_APIKey;
    $registrationIDs = $gcmRegistrationIDs;
    $url = 'https://android.googleapis.com/gcm/send';
    $fields = array(
      'registration_ids'  => $registrationIDs,
      'collapse_key'      => "note"
    );
    $headers = array(
      'Authorization: key=' . $apiKey,
      'Content-Type: application/json'
    );
    $ch = curl_init();
    curl_setopt( $ch, CURLOPT_URL, $url );
    curl_setopt( $ch, CURLOPT_POST, true );
    curl_setopt( $ch, CURLOPT_HTTPHEADER, $headers);
    curl_setopt( $ch, CURLOPT_RETURNTRANSFER, true );
    curl_setopt ($ch, CURLOPT_SSL_VERIFYHOST, 0);
    curl_setopt ($ch, CURLOPT_SSL_VERIFYPEER, 0);
    curl_setopt( $ch, CURLOPT_POSTFIELDS, json_encode($fields) );

    $result = curl_exec($ch);
    if(curl_errno($ch)){ echo 'Curl error: ' . curl_error($ch); }
    curl_close($ch);
    echo $result;
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
