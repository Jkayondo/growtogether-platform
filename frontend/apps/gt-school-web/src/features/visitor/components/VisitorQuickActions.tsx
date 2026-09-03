interface VisitorQuickActionsProps {

    onRegister?: () => void;

    onCheckIn?: () => void;

    onBadges?: () => void;

    onActiveVisitors?: () => void;

}


export default function VisitorQuickActions({

    onRegister,

    onCheckIn,

    onBadges,

    onActiveVisitors

}: VisitorQuickActionsProps) {


    return (

        <div className="gt-card">


            <h3>
                Quick Actions
            </h3>


            <div className="gt-dashboard-summary-grid">


                <button

                    type="button"

                    onClick={onRegister}

                >

                    Register Visitor

                </button>



                <button

                    type="button"

                    onClick={onCheckIn}

                >

                    Check-In Visitor

                </button>



                <button

                    type="button"

                    onClick={onBadges}

                >

                    Manage Badges

                </button>



                <button

                    type="button"

                    onClick={onActiveVisitors}

                >

                    Active Visitors

                </button>


            </div>


        </div>

    );

}
