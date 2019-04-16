import PropTypes from 'prop-types';
import React from 'react';
import {
  Card,
  List,
  Tab,
} from 'semantic-ui-react';


const Predicate = ({ predicateType, args, polarity }) => (
  <Card>
    <Card.Content>
      <Card.Header>{predicateType}</Card.Header>
      { !polarity && <Card.Meta>Negated</Card.Meta> }
      <List>
        {args.map(arg => <List.Item key={arg.type} header={arg.type} content={arg.value} />)}
      </List>
    </Card.Content>
  </Card>
);

const predicateShape = {
  predicateType: PropTypes.string.isRequired,
  args: PropTypes.arrayOf(PropTypes.shape({
    type: PropTypes.string,
    value: PropTypes.string,
  })).isRequired,
  polarity: PropTypes.bool.isRequired,
};

Predicate.propTypes = predicateShape;

const Semantics = ({ events }) => {
  const panes = events.map(event => ({
    menuItem: event.name,
    render: () => (
      <Card.Group>
        {
            event.predicates.map(pred => (
              <Predicate
                key={pred.predicate + pred.type}
                polarity={pred.polarity}
                args={pred.args}
                predicateType={pred.type}
              />
            ))
        }
      </Card.Group>
    ),
  }));
  return <Tab menu={{ fluid: true }} panes={panes} />;
};

Semantics.propTypes = {
  events: PropTypes.arrayOf(PropTypes.shape(
    {
      name: PropTypes.string,
      predicates: predicateShape,
    },
  )).isRequired,
};


export default Semantics;
