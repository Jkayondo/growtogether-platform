import { useState } from "react";
import { useNavigate } from "react-router-dom";

import { useAuth } from "../../auth/authContext";

import {
  saveSession
} from "../../auth/authService";


import {
  login as loginApi
} from "../../auth/authApi";


import {
  Role
} from "../../auth/roles";

import type {
  Role as RoleType
} from "../../auth/roles";


export default function Login() {


  const navigate =
    useNavigate();


  const {
    login
  } = useAuth();



  const [email, setEmail] =
    useState("");



  const [password, setPassword] =
    useState("");



  const [error, setError] =
    useState("");




  const handleLogin = async (
    event: React.FormEvent
  ) => {


    event.preventDefault();


    setError("");



    try {


      const response =
        await loginApi({

          usernameOrEmail:
            email,

          password

        });



      const loginResponse =
        response.data;



      if (loginResponse.mfaRequired) {


        setError(
          "Multi-factor authentication required."
        );


        return;

      }




      const tokens =
        loginResponse.tokens;



      if (!tokens) {


        throw new Error(
          "No authentication token received."
        );

      }




      const backendRole =
        tokens.roles?.[0];



      let role: RoleType =
        Role.SCHOOL_ADMIN;



      if (
        backendRole === Role.TEACHER ||
        backendRole === Role.FINANCE_OFFICER ||
        backendRole === Role.PARENT ||
        backendRole === Role.LEARNER ||
        backendRole === Role.SCHOOL_ADMIN
      ) {

        role =
          backendRole;

      }





      const user = {


        id:
          tokens.userId,


        name:
          tokens.username,


        email,


        organisationId:
          tokens.tenantId,


        organisationName:
          "GT School",


        role,

        permissions:
          tokens.permissions ?? []

      };





      saveSession({

        accessToken:
          tokens.accessToken,

        refreshToken:
          tokens.refreshToken,

        user

      });





      login(user);



      navigate("/");



    }

    catch (error) {


      console.error(
        "Login failed",
        error
      );


      setError(
        "Login failed. Please check your credentials."
      );


    }


  };




  return (

    <div>


      <h2>
        GT School Login
      </h2>



      <form
        onSubmit={handleLogin}
      >


        <div>


          <label>
            Username or Email
          </label>


          <input

            type="text"

            autoComplete="username"

            value={email}

            required

            onChange={
              (e) =>
                setEmail(
                  e.target.value
                )
            }

          />


        </div>




        <div>


          <label>
            Password
          </label>


          <input

            type="password"

            value={password}

            required

            onChange={
              (e) =>
                setPassword(
                  e.target.value
                )
            }

          />


        </div>




        {
          error &&

          <p>
            {error}
          </p>
        }




        <button
          type="submit"
        >

          Login

        </button>



      </form>


    </div>

  );

}