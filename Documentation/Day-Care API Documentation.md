API Documentation
==========

This document outlines the usage of the Day-Care API.

- **add-device.php**
     - Accepts:
          - *deviceID*: unique device identifier
     - Returns:
          - *successful* (boolean): was the new registration request successful?
          - *statusMessage* (string): textual description of the request status
          - *deviceID* (string): the deviceID that was submitted

- **user.php**
  - getaccounttypes

      Returns:
          Array with all available account types.

  - add
    - &*firstname*
    - &*lastname*
    - &*ssn*
    - &*address*
    - &*phone*
    - &*email*
    - &*pass*
    - &*accid* (account type of the user)
      - Returns
        - array(successful[boolean], statusMessage[string])
  - Accepts:
    - *firstName*: first name of the user
    - *lastName*: last name of the user
    - *ssn*: social security number of the user
    - *address*: residential address of the user
    - *phone*: phone number of the user
    - *email*: email address of the user
    - *pass*: password for the user
    - *accid*: account type of the user
  - Returns:
