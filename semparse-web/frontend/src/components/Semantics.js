import PropTypes from 'prop-types';
import React, { Component } from 'react';
import {
  Card,
  Icon,
  List,
  Tab,
  Grid, Button,
} from 'semantic-ui-react';

const PREDICATE_TYPE_MAP = {
  Cause: 'causes',
  'Co Temporal': 'occurs with',
};

const Predicate = ({
  predicateType, args, polarity, handleTabChange,
}) => {
  let descr;
  if (PREDICATE_TYPE_MAP[predicateType]) {
    if (args.length === 2) {
      descr = (
        <span>
          <Button
            compact
            basic
            color="blue"
            onClick={() => handleTabChange(args[0].eventIndex)}
          >
            {args[0].value}
          </Button>
          {' '}
          {PREDICATE_TYPE_MAP[predicateType]}
          {' '}
          <Button
            compact
            basic
            color="blue"
            onClick={() => handleTabChange(args[1].eventIndex)}
          >
            {args[1].value}
          </Button>
        </span>
      );
    }
  }
  return (
    <Card>
      <Card.Content>
        <Card.Header>
          {!polarity && '¬'}
          {predicateType}
        </Card.Header>
        {
            descr || (
            <List>
              {args.map(arg => (
                <List.Item
                  key={arg.type}
                  header={arg.type}
                  content={arg.value ? arg.value : <Icon name="question circle outline" />}
                />
              ))}
            </List>
            )
          }
      </Card.Content>
    </Card>
  );
};

const predicateShape = {
  predicateType: PropTypes.string.isRequired,
  args: PropTypes.arrayOf(PropTypes.shape({
    type: PropTypes.string,
    value: PropTypes.string,
    eventIndex: PropTypes.number,
  })).isRequired,
  polarity: PropTypes.bool.isRequired,
};

const predicateWithFunc = {
  ...predicateShape,
  handleTabChange: PropTypes.func.isRequired,
};

Predicate.propTypes = predicateWithFunc;


class Semantics extends Component {
  state = {
    activeIndex: 0,
  };

  handleTabChange = (e, { activeIndex }) => this.setState({ activeIndex });

  setTab = activeIndex => this.setState({ activeIndex });

  render() {
    const { events, mainEvent } = this.props;
    const { activeIndex } = this.state;
    const panes = events.map(event => ({
      menuItem: event.name,
      render: () => (
        <Card.Group>
          {
              event.predicates.map(pred => (
                <Predicate
                  handleTabChange={this.setTab}
                  key={pred.predicate + pred.predicateType}
                  polarity={pred.polarity}
                  args={pred.args}
                  predicateType={pred.predicateType}
                />
              ))
            }
        </Card.Group>
      ),
    }));
    return (
      <Grid columns="equal" stackable>
        <Grid.Row>
          {
              panes.length > 0
              && (
              <Grid.Column>
                <Tab
                  activeIndex={activeIndex}
                  onTabChange={this.handleTabChange}
                  menu={{ color: 'blue', secondary: true, pointing: true }}
                  panes={panes}
                />
              </Grid.Column>
              )
            }
          {
              mainEvent
              && (
              <Grid.Column>
                <Tab
                  menu={{ secondary: true, pointing: true }}
                  panes={[
                    {
                      menuItem: 'E',
                      render: () => (
                        <Card.Group>
                          {
                                    mainEvent.predicates.map(pred => (
                                      <Predicate
                                        handleTabChange={this.setTab}
                                        key={pred.predicate + pred.predicateType}
                                        polarity={pred.polarity}
                                        args={pred.args}
                                        predicateType={pred.predicateType}
                                      />
                                    ))
                                  }
                        </Card.Group>
                      ),
                    },
                  ]}
                />
              </Grid.Column>
              )
            }
        </Grid.Row>
      </Grid>
    );
  }
}

Semantics.propTypes = {
  events: PropTypes.arrayOf(PropTypes.shape(
    {
      name: PropTypes.string,
      predicates: PropTypes.arrayOf(PropTypes.shape(predicateShape)),
    },
  )).isRequired,
  mainEvent: PropTypes.shape(
    {
      name: PropTypes.string,
      predicates: PropTypes.arrayOf(PropTypes.shape(predicateShape)),
    },
  ),
};

Semantics.defaultProps = {
  mainEvent: undefined,
};


export default Semantics;
