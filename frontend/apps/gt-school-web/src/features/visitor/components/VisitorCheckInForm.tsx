import { useState } from "react";

import {
    checkInVisitor
} from "../../../services/visitorApi";


export default function VisitorCheckInForm() {


    const [visitorName, setVisitorName] =
        useState("");


    const [purpose, setPurpose] =
        useState("");


    const [message, setMessage] =
        useState("");



    const [loading, setLoading] =
        useState(false);




    async function submit() {


        setLoading(true);

        setMessage("");



        try {


            const response =
                await checkInVisitor({

                    visitorName,

                    purpose

                });



            setMessage(
                `Visitor checked in: ${response.visitorName}`
            );



            setVisitorName("");

            setPurpose("");



        } catch (error) {


            setMessage(
                "Unable to check in visitor."
            );


        } finally {


            setLoading(false);


        }


    }



    return (

        <div className="gt-card">


            <h3>
                Visitor Check-In
            </h3>



            <input

                className="gt-input"

                placeholder="Visitor name"

                value={visitorName}

                onChange={
                    (e) =>
                        setVisitorName(
                            e.target.value
                        )
                }

            />



            <input

                className="gt-input"

                placeholder="Purpose of visit"

                value={purpose}

                onChange={
                    (e) =>
                        setPurpose(
                            e.target.value
                        )
                }

            />



            <button

                className="gt-button"

                disabled={loading}

                onClick={submit}

            >

                {
                    loading
                        ? "Checking In..."
                        : "Check In Visitor"
                }


            </button>



            {
                message && (

                    <p>
                        {message}
                    </p>

                )
            }


        </div>

    );

}