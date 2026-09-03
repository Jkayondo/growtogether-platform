import GTLogo from "../../assets/GT logo/GT logo landscape.svg?react";
import { useAuth } from "../../auth/authContext";


export default function GTHeader() {

  const {
    user,
    logout
  } = useAuth();



  return (

    <header>


      <div className="gt-platform-brand">

        <GTLogo className="gt-logo" />

        <span>
          School Administration Portal
        </span>

      </div>



      <div className="gt-school-identity">

        <div className="gt-school-badge">
          🏫
        </div>


        <div>

          <strong>
            {user?.organisationName ??
              "GT School"}
          </strong>


          <small>
            Administrator Portal
          </small>

        </div>


      </div>




      <div className="gt-user-profile">


        <div className="gt-user-avatar">
          👤
        </div>


        <div>

          <strong>
            {user?.name ??
              "Guest User"}
          </strong>


          <small>
            {user?.role ??
              "Visitor"}
          </small>

        </div>



        {
          user && (

            <button
              onClick={logout}
            >
              Logout
            </button>

          )
        }


      </div>


    </header>

  );

}
