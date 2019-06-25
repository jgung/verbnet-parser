import { connect } from 'react-redux';
import _ from 'lodash';

import { getDemoState } from '../selectors';
import * as actions from '../actions';
import ParsingDemo from '../../components/ParsingDemo';


const MAX_LENGTH = 256;

let examplesIndex = 0;

const examples = _.shuffle([
  'If you liked the music we were playing last night, you will absolutely love what we\'re playing tomorrow!',
  'I didn\'t want to spend the next thirty years writing about bad things happening in the same small town',
  'John ate an apple and dropped the core into the trash',
  'Alice sold Bob her laptop for $300',
  'Where did John take the train after leaving the office?',
  'To whom did John lend his guitar last week?',
  'Alice went to the corner shop to buy some eggs, milk and bread for breakfast',
  'Mary discovered the impending layoffs from taking a look at her boss\'s computer',
  'In 2018, crude futures rose 74 cents, or 1.4 percent, to $53.95 from $53.21',
]);

function randomExample() {
  const result = examples[examplesIndex % examples.length];
  examplesIndex += 1;
  return result;
}


const mapStateToProps = state => ({
  ...getDemoState(state),
});

const mapDispatchToProps = dispatch => (
  {
    setUtterance: (e) => {
      const { value } = e.target;
      dispatch(actions.setUtterance(value));
    },
    handleTabChange: (e, { activeIndex }) => {
      dispatch(actions.setTabIndex(activeIndex));
    },
    submitUtterance: (utterance) => {
      const inputUtterance = !utterance ? randomExample() : utterance;

      if (inputUtterance.length > MAX_LENGTH) {
        dispatch(actions.submitUtteranceFailure(
          `Sorry, the maximum sentence length is currently ${MAX_LENGTH} characters. Please try a shorter sentence.`,
        ));
      } else {
        dispatch(actions.submitUtteranceRequest(utterance));
        fetch(`/predict/semantics?utterance=${encodeURIComponent(inputUtterance)}`)
          .then((response) => {
            if (response.ok) {
              return response.json();
            }
            throw new Error('Something went wrong');
          },
          () => {
            dispatch(actions.submitUtteranceFailure("Sorry, we're unable to make predictions at this time."));
          })
          .then((message) => {
            dispatch(actions.submitUtteranceSuccess(message));
          })
          .catch(() => {
            dispatch(actions.submitUtteranceFailure('Sorry, something went wrong and we were unable to handle your request. Please try again later.'));
          });
      }
    },
    togglePropBank: () => dispatch(actions.togglePropbank()),
    toggleVerbNet: () => dispatch(actions.toggleVerbnet()),
    toggleSemantics: () => dispatch(actions.toggleSemantics()),
    toggleFunctionalSemantics: () => dispatch(actions.toggleIndexView()),
    toggleOptions: () => dispatch(actions.toggleViewOptions()),
    toggleModifiers: () => dispatch(actions.toggleModifiers()),
  }
);

export default connect(mapStateToProps, mapDispatchToProps)(ParsingDemo);
