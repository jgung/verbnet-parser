import PropTypes from 'prop-types';
import React, { Component } from 'react';
import {
  Container,
  Icon,
  Menu,
  Responsive,
  Segment,
  Visibility,
} from 'semantic-ui-react';
import HomepageHeading from './HomepageHeading';
import getWidth from '../util/utilities';

class DesktopContainer extends Component {
    state = {
      fixed: false,
    };

    hideFixedMenu = () => this.setState({ fixed: false });

    showFixedMenu = () => this.setState({ fixed: true });

    render() {
      const { children, menuItems } = this.props;
      const { fixed } = this.state;

      return (
        <Responsive getWidth={getWidth} minWidth={Responsive.onlyTablet.minWidth}>
          <Visibility
            once={false}
            onBottomPassed={this.showFixedMenu}
            onBottomPassedReverse={this.hideFixedMenu}
          >
            <Segment
              inverted
              textAlign="left"
              style={{ minHeight: 700, padding: '1em 0em' }}
              vertical
            >
              <Menu
                fixed={fixed ? 'top' : null}
                inverted={!fixed}
                pointing={!fixed}
                secondary={!fixed}
                size="large"
              >
                <Container>
                  { menuItems }
                  <Menu.Item position="right" as="a" href="https://github.com/jgung/verbnet-parser">
                    <Icon name="github" />
                    {' '}
                                    GitHub
                  </Menu.Item>
                </Container>
              </Menu>
              <HomepageHeading />
            </Segment>
          </Visibility>
          {children}
        </Responsive>
      );
    }
}

DesktopContainer.propTypes = {
  children: PropTypes.node.isRequired,
  menuItems: PropTypes.arrayOf(PropTypes.node).isRequired,
};

export default DesktopContainer;

