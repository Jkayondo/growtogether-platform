import { useEffect, useState } from "react";

import {
    getVisitorBadges,
    type VisitorBadge
} from "../../../services/visitorApi";

import VisitorStatusBadge
    from "./VisitorStatusBadge";



export default function VisitorBadgePanel() {


    const [badges, setBadges] =
        useState<VisitorBadge[]>([]);



    const [loading, setLoading] =
        useState(true);



    const [error, setError] =
        useState<string | null>(null);




    useEffect(() => {


        getVisitorBadges()

            .then((data) => {


                setBadges(data);


            })

            .catch((err) => {


                setError(
                    err.message
                );


            })

            .finally(() => {


                setLoading(false);


            });



    }, []);





    if (loading) {

        return (

            <div className="gt-card">

                Loading visitor badges...

            </div>

        );

    }





    if (error) {

        return (

            <div className="gt-card">

                Unable to load visitor badges.

            </div>

        );

    }





    return (

        <div className="gt-card">


            <h3>

                Visitor Badges

            </h3>




            {
                badges.length === 0 && (

                    <p>

                        No visitor badges found.

                    </p>

                )
            }





            {
                badges.map(

                    (badge) => (

                        <div

                            key={badge.id}

                            className="gt-activity-item"

                        >

                            <strong>

                                Badge:
                                {" "}
                                {badge.badgeNumber}

                            </strong>



                            <p>

                                Visitor:
                                {" "}
                                {badge.visitorName}

                            </p>



                            <p>

                                Type:
                                {" "}
                                {badge.badgeType}

                            </p>



                            <p>

                                Status:

                                {" "}

                                <VisitorStatusBadge

                                    status={
                                        badge.badgeStatus
                                    }

                                />

                            </p>



                        </div>

                    )

                )
            }



        </div>

    );

}