interface VisitorSecurityOverviewProps {

    pendingRequests: number;

    activeVisitors: number;

    activeBadges: number;

    unreturnedBadges: number;

}


export default function VisitorSecurityOverview({

    pendingRequests,

    activeVisitors,

    activeBadges,

    unreturnedBadges

}: VisitorSecurityOverviewProps) {


    return (

        <div className="gt-card">


            <h3>
                Visitor Security Overview
            </h3>


            <div className="gt-dashboard-summary-grid">


                <div className="gt-panel">

                    <strong>
                        Pending Requests
                    </strong>

                    <h2>
                        {pendingRequests}
                    </h2>

                </div>



                <div className="gt-panel">

                    <strong>
                        Visitors Inside
                    </strong>

                    <h2>
                        {activeVisitors}
                    </h2>

                </div>



                <div className="gt-panel">

                    <strong>
                        Active Badges
                    </strong>

                    <h2>
                        {activeBadges}
                    </h2>

                </div>



                <div className="gt-panel">

                    <strong>
                        Unreturned Badges
                    </strong>

                    <h2>
                        {unreturnedBadges}
                    </h2>

                </div>


            </div>


        </div>

    );

}
