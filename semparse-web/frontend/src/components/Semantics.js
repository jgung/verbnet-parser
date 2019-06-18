import PropTypes from 'prop-types';
import React, { Component } from 'react';
import {
  Card,
  Icon,
  List,
  Tab,
  Grid, Button, Segment,
} from 'semantic-ui-react';

const PREDICATE_TYPE_MAP = {
  Cause: { nl: 'causes', functional: 'cause' },
  'Co Temporal': { nl: 'occurs with', functional: 'co temporal' },
  Overlaps: { nl: 'overlaps with', functional: 'overlaps' },
  Meets: { nl: 'meets', functional: 'meets' },
  'Repeated Sequence': { nl: 'repeats with', functional: 'repeated sequence' },
  'Repeat Sequence': { nl: 'repeats with', functional: 'repeated sequence' },
};

const Predicate = ({
  predicateType, args, polarity, handleTabChange, functionalView,
}) => {
  let descr;
  if (PREDICATE_TYPE_MAP[predicateType]) {
    if (args.length === 2 && !functionalView) {
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
          {PREDICATE_TYPE_MAP[predicateType].nl}
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
    } else if (functionalView) {
      descr = (
        <span>
          <b>
            {PREDICATE_TYPE_MAP[predicateType].functional.toUpperCase().replace(' ', '_')}
          </b>
            (
          {
                args
                  .map(a => (
                    <Button
                      key={a.type + a.value}
                      compact
                      primary
                      basic
                      onClick={() => handleTabChange(a.eventIndex)}
                    >
                      {a.value}
                    </Button>
                  ))
                  .reduce((prev, curr) => [prev, ', ', curr])
            }
            )
        </span>
      );
    }
  }

  if (functionalView) {
    return (
      <Segment style={{ fontSize: '1.2rem' }}>
        {
                  descr || (
                  <span>
                    <b>
                      {!polarity && '¬'}
                      {predicateType.toUpperCase().replace(' ', '_')}
                    </b>
                  (
                    {' '}
                    {args
                      .map(a => (
                        <span key={a.type + a.value}>
                          {a.value ? <i style={{ fontSize: '1.1rem' }}>{a.value}</i> : <Icon name="question circle outline" />}
                          {' '}
                          <sub><b>{a.type.replace(' ', '_')}</b></sub>
                        </span>
                      ))
                      .reduce((prev, curr) => [prev, ' ,  ', curr])}
                    {' '}
                          )
                  </span>
                  )
              }
      </Segment>
    );
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
  functionalView: PropTypes.bool.isRequired,
};

Predicate.propTypes = predicateWithFunc;


class Semantics extends Component {
  state = {
    activeIndex: 0,
  };

  handleTabChange = (e, { activeIndex }) => this.setState({ activeIndex });

  setTab = activeIndex => this.setState({ activeIndex });

  render() {
    const { events, mainEvent, functionalView } = this.props;
    const { activeIndex } = this.state;
    const panes = events.map(event => ({
      menuItem: event.name,
      render: () => {
        const predicates = event.predicates.map(pred => (
          <Predicate
            handleTabChange={this.setTab}
            key={pred.predicate + pred.predicateType}
            polarity={pred.polarity}
            args={pred.args}
            predicateType={pred.predicateType}
            functionalView={functionalView}
          />
        ));
        return functionalView ? <div>{predicates}</div> : <Card.Group>{predicates}</Card.Group>;
      }
      ,
    }));

    const predicates = mainEvent && mainEvent.predicates.map(pred => (
      <Predicate
        handleTabChange={this.setTab}
        key={pred.predicate + pred.predicateType}
        polarity={pred.polarity}
        args={pred.args}
        predicateType={pred.predicateType}
        functionalView={functionalView}
      />
    ));
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
          {!mainEvent && <Grid.Column />}
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
                        functionalView ? <div>{predicates}</div> : (
                          <Card.Group>
                            { predicates }
                          </Card.Group>
                        )
                      ),
                    },
                  ]}
                />
              </Grid.Column>
              )
            }
          {panes.length === 0 && <Grid.Column />}

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
  functionalView: PropTypes.bool.isRequired,
};

Semantics.defaultProps = {
  mainEvent: undefined,
};


export default Semantics;
