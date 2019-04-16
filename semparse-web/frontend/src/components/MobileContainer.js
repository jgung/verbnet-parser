import PropTypes from 'prop-types';
import React, { Component } from 'react';
import {
  Container,
  Icon,
  Menu,
  Responsive,
  Segment,
  Sidebar,
} from 'semantic-ui-react';
import HomepageHeading from './HomepageHeading';
import getWidth from '../util/utilities';

class MobileContainer extends Component {
    state = {
      sidebarOpened: false,
    };

    handleSidebarHide = () => this.setState({ sidebarOpened: false });

    handleToggle = () => this.setState({ sidebarOpened: true });

    render() {
      const { children, menuItems } = this.props;
      const { sidebarOpened } = this.state;

      return (
        <Responsive
          as={Sidebar.Pushable}
          getWidth={getWidth}
          maxWidth={Responsive.onlyMobile.maxWidth}
        >
          <Sidebar
            as={Menu}
            animation="push"
            inverted
            onHide={this.handleSidebarHide}
            vertical
            visible={sidebarOpened}
            content={menuItems}
          />

          <Sidebar.Pusher dimmed={sidebarOpened}>
            <Segment
              inverted
              textAlign="center"
              style={{ minHeight: 350, padding: '1em 0em' }}
              vertical
            >
              <Container>
                <Menu inverted pointing secondary size="large">
                  <Menu.Item onClick={this.handleToggle}>
                    <Icon name="sidebar" />
                  </Menu.Item>
                  <Menu.Item position="right" as="a" href="https://github.com/jgung/verbnet-parser">
                    <Icon name="github" />
                    {' '}
                                    GitHub
                  </Menu.Item>
                </Menu>
              </Container>
              <HomepageHeading mobile />
            </Segment>
            {children}
          </Sidebar.Pusher>
        </Responsive>
      );
    }
}

MobileContainer.propTypes = {
  children: PropTypes.node.isRequired,
  menuItems: PropTypes.arrayOf(PropTypes.node).isRequired,
};

export default MobileContainer;
