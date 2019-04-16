import PropTypes from 'prop-types';
import React from 'react';
import {
  Divider,
  Tab,
} from 'semantic-ui-react';
import RoleLabels from './RoleLabels';
import Semantics from './Semantics';

const Proposition = ({
  sense, propBankRoles, verbNetRoles, events,
}) => (
  <div>
    { propBankRoles && <RoleLabels roles={propBankRoles} sense={sense} /> }
    { propBankRoles && <Divider hidden /> }
    { verbNetRoles && <RoleLabels roles={verbNetRoles} sense={sense} /> }
    { (propBankRoles || verbNetRoles) && <Divider hidden /> }
    { events && <Semantics events={events} /> }
  </div>
);

const propositionTypes = {
  sense: PropTypes.string.isRequired,
  propBankRoles: PropTypes.arrayOf(PropTypes.object).isRequired,
  verbNetRoles: PropTypes.arrayOf(PropTypes.object).isRequired,
  events: PropTypes.arrayOf(PropTypes.object).isRequired,
};

Proposition.propTypes = propositionTypes;

const Propositions = ({
  propositions, propIndex, handleTabChange, showPropBank, showVerbNet, showSemantics,
}) => {
  const propPanes = propositions.map((prop) => {
    const {
      sense, propBankSpans, verbNetSpans, events,
    } = prop;
    return {
      menuItem: sense,
      render: () => (
        <Tab.Pane key={sense} attached={false}>
          <Proposition
            propBankRoles={showPropBank && propBankSpans}
            verbNetRoles={showVerbNet && verbNetSpans}
            events={showSemantics && events}
            sense={sense}
          />
        </Tab.Pane>
      ),
    };
  });

  return (
    <Tab
      menu={{ secondary: true, pointing: true }}
      activeIndex={propIndex}
      onTabChange={handleTabChange}
      panes={propPanes}
    />
  );
};

Propositions.propTypes = {
  propositions: PropTypes.arrayOf(PropTypes.shape(propositionTypes)).isRequired,
  propIndex: PropTypes.number.isRequired,
  showPropBank: PropTypes.bool.isRequired,
  showVerbNet: PropTypes.bool.isRequired,
  showSemantics: PropTypes.bool.isRequired,
  handleTabChange: PropTypes.func.isRequired,
};


export default Propositions;
